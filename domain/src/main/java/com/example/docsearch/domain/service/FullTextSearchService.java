package com.example.docsearch.domain.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.LuceneFields;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import com.example.docsearch.domain.model.SearchHit;

/**
 * <p>Lucene 索引に対する全文検索。本文・ファイル名の両方を対象に検索する。</p>
 *
 * <p>一致箇所は本文抜粋（スニペット）として {@code <mark>} で強調する。スニペットは
 * HTMLエスケープ済みで、強調タグ以外の HTML は含まない。</p>
 */
@Service
public class FullTextSearchService {

    /** 1回の検索で返す最大件数。 */
    public static final int DEFAULT_LIMIT = 200;

    /** 抜粋の最大長（フォールバック時）。 */
    private static final int SNIPPET_LENGTH = 160;
    // ハイライト用の内部マーカー。通常の文書に出現せず、HTMLエスケープの影響を受けない
    // ASCII 文字列を使い、エスケープ後に <mark> へ置換する。
    private static final String HL_PRE = "@@DSMARK_OPEN@@";
    private static final String HL_POST = "@@DSMARK_CLOSE@@";

    private final AppPaths appPaths;
    private final Analyzer analyzer = new JapaneseAnalyzer();

    public FullTextSearchService(AppPaths appPaths) {
        this.appPaths = appPaths;
    }

    /**
     * <p>既定の上限件数で全文検索する。</p>
     *
     * @param query 検索語
     * @return ヒット一覧（スコア降順）
     */
    public List<SearchHit> search(String query) {
        return search(query, DEFAULT_LIMIT);
    }

    /**
     * <p>上限件数を指定して全文検索する。</p>
     *
     * @param query 検索語
     * @param limit 返す最大件数
     * @return ヒット一覧（スコア降順）
     */
    public List<SearchHit> search(String query, int limit) {
        List<SearchHit> hits = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return hits;
        }
        try (FSDirectory dir = FSDirectory.open(appPaths.indexDir())) {
            if (!DirectoryReader.indexExists(dir)) {
                return hits; // 未索引
            }
            try (DirectoryReader reader = DirectoryReader.open(dir)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query parsed = buildQuery(query.strip());

                TopDocs top = searcher.search(parsed, limit);
                StoredFields storedFields = searcher.storedFields();

                QueryScorer scorer = new QueryScorer(parsed, LuceneFields.CONTENT);
                Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(HL_PRE, HL_POST), scorer);
                highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, SNIPPET_LENGTH));

                for (ScoreDoc scoreDoc : top.scoreDocs) {
                    Document doc = storedFields.document(scoreDoc.doc);
                    hits.add(toHit(doc, scoreDoc.score, highlighter));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("検索に失敗しました", e);
        } catch (ParseException e) {
            throw new IllegalArgumentException("検索語の解析に失敗しました: " + query, e);
        }
        return hits;
    }

    private Query buildQuery(String rawQuery) throws ParseException {
        String[] fields = {LuceneFields.CONTENT, LuceneFields.NAME};
        Map<String, Float> boosts = Map.of(
                LuceneFields.CONTENT, 1.0f,
                LuceneFields.NAME, 2.0f
        );
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        // ユーザー入力は記号をエスケープし、プレーンな語として扱う
        return parser.parse(QueryParser.escape(rawQuery));
    }

    private SearchHit toHit(Document doc, float score, Highlighter highlighter) {
        String fileName = nullToEmpty(doc.get(LuceneFields.NAME));
        String path = nullToEmpty(doc.get(LuceneFields.PATH));
        String parent = nullToEmpty(doc.get(LuceneFields.PARENT));
        String extension = nullToEmpty(doc.get(LuceneFields.EXTENSION));
        long size = numeric(doc.getField(LuceneFields.SIZE));
        long modified = numeric(doc.getField(LuceneFields.MODIFIED));
        String snippet = makeSnippet(highlighter, doc.get(LuceneFields.CONTENT));
        return new SearchHit(fileName, path, parent, extension, size, modified, score, snippet);
    }

    private String makeSnippet(Highlighter highlighter, String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        try {
            String best = highlighter.getBestFragment(analyzer, LuceneFields.CONTENT, content);
            if (best != null && !best.isBlank()) {
                return decorate(best);
            }
        } catch (Exception e) {
            // ハイライト失敗時は先頭抜粋にフォールバック
        }
        String plain = content.strip();
        String head = plain.length() > SNIPPET_LENGTH ? plain.substring(0, SNIPPET_LENGTH) + "…" : plain;
        return escapeHtml(head);
    }

    /**
     * <p>マーカーを保持したまま HTML エスケープし、最後に {@code <mark>} へ置換する。</p>
     */
    private String decorate(String fragment) {
        return escapeHtml(fragment)
                .replace(HL_PRE, "<mark>")
                .replace(HL_POST, "</mark>");
    }

    private static String escapeHtml(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static long numeric(IndexableField field) {
        return (field == null || field.numericValue() == null) ? 0L : field.numericValue().longValue();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
