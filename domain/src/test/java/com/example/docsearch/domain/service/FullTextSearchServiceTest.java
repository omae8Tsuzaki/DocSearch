package com.example.docsearch.domain.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.DomainConfig;
import com.example.docsearch.domain.model.SearchHit;
import com.example.docsearch.domain.service.lucene.LuceneAnalyzerConfig;
import com.example.docsearch.domain.service.lucene.LuceneFields;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>{@link FullTextSearchService} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #searchSuccess01} 正常系：拡張子を指定しない場合、全ての拡張子のファイルがヒットすることを確認する。</li>
 *     <li>{@link #searchSuccess02} 正常系：拡張子を指定した場合、その拡張子のファイルのみがヒットすることを確認する。</li>
 *     <li>{@link #searchSuccess03} 正常系：拡張子に一致するファイルがない場合、ヒットなしになることを確認する。</li>
 *     <li>{@link #listExtensionsSuccess01} 正常系：索引済みの拡張子一覧が重複なく昇順で取得できることを確認する。</li>
 *     <li>{@link #listExtensionsSuccess02} 正常系：未索引の場合、空リストが返却されることを確認する。</li>
 * </ul>
 */
@SpringBootTest(properties = "spring.config.import=classpath:domain-config.yml")
@ContextConfiguration(classes = {DomainConfig.class, LuceneAnalyzerConfig.class})
public class FullTextSearchServiceTest {

    @Autowired
    private DomainConfig config;

    @Autowired
    private Analyzer analyzer;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[search]
        テスト観点：拡張子を指定しない場合、全ての拡張子のファイルがヒットすることを確認する。
        """)
    public void searchSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        buildIndex(appPaths,
                doc("C:\\docs\\report.txt", "report", "議事録の本文です", "txt"),
                doc("C:\\docs\\summary.pdf", "summary", "議事録の要約です", "pdf"));
        FullTextSearchService service = new FullTextSearchService(appPaths, config, analyzer);

        //
        // 実行
        //
        List<SearchHit> result = service.search("議事録");

        //
        // 検証
        //
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[search]
        テスト観点：拡張子を指定した場合、その拡張子のファイルのみがヒットすることを確認する。
        """)
    public void searchSuccess02() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        buildIndex(appPaths,
                doc("C:\\docs\\report.txt", "report", "議事録の本文です", "txt"),
                doc("C:\\docs\\summary.pdf", "summary", "議事録の要約です", "pdf"));
        FullTextSearchService service = new FullTextSearchService(appPaths, config, analyzer);

        //
        // 実行
        //
        List<SearchHit> result = service.search("議事録", config.getSearchMaxLimit(), "pdf");

        //
        // 検証
        //
        assertEquals(1, result.size());
        assertEquals("pdf", result.getFirst().extension());
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[search]
        テスト観点：拡張子に一致するファイルがない場合、ヒットなしになることを確認する。
        """)
    public void searchSuccess03() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        buildIndex(appPaths, doc("C:\\docs\\report.txt", "report", "議事録の本文です", "txt"));
        FullTextSearchService service = new FullTextSearchService(appPaths, config, analyzer);

        //
        // 実行
        //
        List<SearchHit> result = service.search("議事録", config.getSearchMaxLimit(), "docx");

        //
        // 検証
        //
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[listExtensions]
        テスト観点：索引済みの拡張子一覧が重複なく昇順で取得できることを確認する。
        """)
    public void listExtensionsSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        buildIndex(appPaths,
                doc("C:\\docs\\a.pdf", "a", "内容A", "pdf"),
                doc("C:\\docs\\b.txt", "b", "内容B", "txt"),
                doc("C:\\docs\\c.pdf", "c", "内容C", "pdf"));
        FullTextSearchService service = new FullTextSearchService(appPaths, config, analyzer);

        //
        // 実行
        //
        List<String> result = service.listExtensions();

        //
        // 検証
        //
        assertEquals(List.of("pdf", "txt"), result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[listExtensions]
        テスト観点：未索引の場合、空リストが返却されることを確認する。
        """)
    public void listExtensionsSuccess02() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        FullTextSearchService service = new FullTextSearchService(appPaths, config, analyzer);

        //
        // 実行
        //
        List<String> result = service.listExtensions();

        //
        // 検証
        //
        assertTrue(result.isEmpty());
    }

    private record Doc(String path, String name, String content, String extension) {
    }

    private static Doc doc(String path, String name, String content, String extension) {
        return new Doc(path, name, content, extension);
    }

    private void buildIndex(AppPaths appPaths, Doc... docs) throws IOException {
        try (FSDirectory dir = FSDirectory.open(appPaths.indexDir());
             IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(analyzer))) {
            for (Doc d : docs) {
                Document document = new Document();
                document.add(new StringField(LuceneFields.PATH, d.path(), Field.Store.YES));
                document.add(new TextField(LuceneFields.NAME, d.name(), Field.Store.YES));
                document.add(new TextField(LuceneFields.CONTENT, d.content(), Field.Store.YES));
                document.add(new StringField(LuceneFields.EXTENSION, d.extension(), Field.Store.YES));
                document.add(new StoredField(LuceneFields.PARENT, "C:\\docs"));
                document.add(new StoredField(LuceneFields.SIZE, 100L));
                document.add(new StoredField(LuceneFields.MODIFIED, System.currentTimeMillis()));
                writer.addDocument(document);
            }
            writer.commit();
        }
    }
}
