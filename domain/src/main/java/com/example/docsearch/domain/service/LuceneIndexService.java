package com.example.docsearch.domain.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.LuceneFields;
import com.example.docsearch.domain.TextExtractor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import com.example.docsearch.domain.model.IndexResult;
import com.example.docsearch.domain.model.IndexStatus;

/**
 * <p>検索対象フォルダ配下を走査し、Lucene 索引を作成・更新する。</p>
 *
 * <p>再索引はバックグラウンドスレッドで非同期に実行し、HTTP タイムアウトを避ける。
 * 更新日時が変わっていないファイルはスキップする差分索引方式とし、削除済みファイルは
 * 索引から取り除く。</p>
 */
@Service
public class LuceneIndexService implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexService.class);

    private final AppPaths appPaths;
    private final TextExtractor extractor;
    private final Analyzer analyzer = new JapaneseAnalyzer();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "docsearch-indexer");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean indexing = new AtomicBoolean(false);

    public LuceneIndexService(AppPaths appPaths, TextExtractor extractor) {
        this.appPaths = appPaths;
        this.extractor = extractor;
    }

    /**
     * <p>再索引をバックグラウンドで開始する。</p>
     *
     * @param folders 検索対象フォルダ
     * @return 開始できたら true。既に実行中なら false。
     */
    public boolean startReindex(List<String> folders) {
        if (!indexing.compareAndSet(false, true)) {
            return false;
        }
        executor.submit(() -> {
            try {
                IndexResult result = doReindex(folders);
                writeLastIndexed(System.currentTimeMillis());
                LOGGER.info("索引完了 indexed={} skipped={} removed={} failed={} {}ms",
                        result.indexed(), result.skipped(), result.removed(), result.failed(), result.elapsedMs());
            } catch (Exception e) {
                LOGGER.error("索引作成に失敗しました", e);
            } finally {
                indexing.set(false);
            }
        });
        return true;
    }

    /**
     * <p>索引の現在状態を返す。</p>
     *
     * @return 索引状態
     */
    public IndexStatus status() {
        return new IndexStatus(indexing.get(), docCount(), readLastIndexed());
    }

    private IndexResult doReindex(List<String> folders) throws IOException {
        long startNs = System.nanoTime();
        int[] counts = new int[3]; // [0]=indexed, [1]=skipped, [2]=failed
        int removed = 0;

        try (FSDirectory dir = FSDirectory.open(appPaths.indexDir())) {
            Map<String, Long> existing = readExistingModified(dir);
            Set<String> seen = new HashSet<>();

            IndexWriterConfig config = new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE_OR_APPEND);
            try (IndexWriter writer = new IndexWriter(dir, config)) {
                for (String folder : folders) {
                    Path root = Path.of(folder);
                    if (!Files.isDirectory(root)) {
                        continue;
                    }
                    indexFolder(writer, root, existing, seen, counts);
                }

                // 既存索引にあるが今回見つからなかった（＝削除された）ファイルを除去
                for (String path : existing.keySet()) {
                    if (!seen.contains(path)) {
                        writer.deleteDocuments(new Term(LuceneFields.PATH, path));
                        removed++;
                    }
                }
                writer.commit();
            }
        }

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        return new IndexResult(counts[0], counts[1], removed, counts[2], elapsedMs);
    }

    private void indexFolder(IndexWriter writer, Path root,
                             Map<String, Long> existing, Set<String> seen, int[] counts) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    String path = file.toAbsolutePath().toString();
                    seen.add(path);
                    long modified = attrs.lastModifiedTime().toMillis();

                    Long prev = existing.get(path);
                    if (prev != null && prev.longValue() == modified) {
                        counts[1]++; // 更新なし → スキップ
                        return FileVisitResult.CONTINUE;
                    }

                    long size = attrs.size();
                    String content = extractor.extract(file, size);
                    writer.updateDocument(new Term(LuceneFields.PATH, path),
                            buildDocument(file, path, content, size, modified));
                    counts[0]++;
                } catch (IOException e) {
                    counts[2]++;
                    LOGGER.debug("索引化に失敗（スキップ）: {}", file, e);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // アクセス不可などはスキップして走査を続行
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Document buildDocument(Path file, String path, String content, long size, long modified) {
        Path namePath = file.getFileName();
        String fileName = namePath == null ? path : namePath.toString();
        Path parent = file.getParent();

        Document doc = new Document();
        doc.add(new StringField(LuceneFields.PATH, path, Field.Store.YES));
        doc.add(new TextField(LuceneFields.NAME, fileName, Field.Store.YES));
        doc.add(new TextField(LuceneFields.CONTENT, content, Field.Store.YES));
        doc.add(new StringField(LuceneFields.EXTENSION, extensionOf(fileName), Field.Store.YES));
        doc.add(new StoredField(LuceneFields.PARENT, parent == null ? "" : parent.toString()));
        doc.add(new StoredField(LuceneFields.SIZE, size));
        doc.add(new StoredField(LuceneFields.MODIFIED, modified));
        return doc;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0 && dot < fileName.length() - 1)
                ? fileName.substring(dot + 1).toLowerCase(Locale.ROOT)
                : "";
    }

    private Map<String, Long> readExistingModified(FSDirectory dir) throws IOException {
        Map<String, Long> map = new HashMap<>();
        if (!DirectoryReader.indexExists(dir)) {
            return map;
        }
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            StoredFields storedFields = reader.storedFields();
            Bits liveDocs = MultiBits.getLiveDocs(reader);
            for (int i = 0; i < reader.maxDoc(); i++) {
                if (liveDocs != null && !liveDocs.get(i)) {
                    continue;
                }
                Document doc = storedFields.document(i);
                String path = doc.get(LuceneFields.PATH);
                IndexableField modified = doc.getField(LuceneFields.MODIFIED);
                if (path != null && modified != null && modified.numericValue() != null) {
                    map.put(path, modified.numericValue().longValue());
                }
            }
        }
        return map;
    }

    private int docCount() {
        try (FSDirectory dir = FSDirectory.open(appPaths.indexDir())) {
            if (!DirectoryReader.indexExists(dir)) {
                return 0;
            }
            try (DirectoryReader reader = DirectoryReader.open(dir)) {
                return reader.numDocs();
            }
        } catch (IOException e) {
            return 0;
        }
    }

    private long readLastIndexed() {
        Path meta = appPaths.indexMetaFile();
        if (!Files.exists(meta)) {
            return 0L;
        }
        try {
            String text = Files.readString(meta, StandardCharsets.UTF_8).strip();
            return text.isEmpty() ? 0L : Long.parseLong(text);
        } catch (IOException | NumberFormatException e) {
            return 0L;
        }
    }

    private void writeLastIndexed(long epochMs) {
        try {
            Files.writeString(appPaths.indexMetaFile(), Long.toString(epochMs), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("索引メタ情報の保存に失敗しました", e);
        }
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
