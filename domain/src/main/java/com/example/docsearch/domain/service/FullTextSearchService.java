package com.example.docsearch.domain.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.docsearch.core.exception.ApplicationException;
import com.example.docsearch.core.exception.ServiceException;
import com.example.docsearch.core.util.HtmlEscaper;
import com.example.docsearch.core.util.StringUtils;
import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.DomainConfig;
import com.example.docsearch.domain.service.lucene.LuceneFields;
import com.example.docsearch.domain.service.lucene.LuceneReaders;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
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

    /** 抜粋の最大長（フォールバック時）。 */
    private static final int SNIPPET_LENGTH = 160;
    // ハイライト用の内部マーカー。通常の文書に出現せず、HTMLエスケープの影響を受けない
    // ASCII 文字列を使い、エスケープ後に <mark> へ置換する。
    private static final String HL_PRE = "@@DSMARK_OPEN@@";
    private static final String HL_POST = "@@DSMARK_CLOSE@@";

    private final AppPaths appPaths;
    // １回の検索で返す最大件数（設定値）
    private final int maxLimit;
    private final Analyzer analyzer;

    public FullTextSearchService(AppPaths appPaths, DomainConfig domainConfig, Analyzer analyzer) {
        this.appPaths = appPaths;
        this.maxLimit = domainConfig.getSearchMaxLimit();
        this.analyzer = analyzer;
    }

    /**
     * <p>既定の上限件数で全文検索する。</p>
     *
     * @param query 検索語
     * @return ヒット一覧（スコア降順）
     */
    public List<SearchHit> search(String query) {
        return search(query, this.maxLimit, "");
    }

    /**
     * <p>上限件数を指定して全文検索する。</p>
     *
     * @param query 検索語
     * @param limit 返す最大件数
     * @return ヒット一覧（スコア降順）
     */
    public List<SearchHit> search(String query, int limit) {
        return search(query, limit, "");
    }

    /**
     * <p>上限件数と拡張子絞り込みを指定して全文検索する。</p>
     *
     * @param query     検索語
     * @param limit     返す最大件数
     * @param extension 絞り込む拡張子（小文字・ドットなし）。{@code null} または空文字なら絞り込まない
     * @return ヒット一覧（スコア降順）
     */
    public List<SearchHit> search(String query, int limit, String extension) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            Query parsed = buildQuery(query.strip(), extension);
            try (FSDirectory dir = FSDirectory.open(appPaths.indexDir())) {
                return LuceneReaders.withReader(dir, List.<SearchHit>of(),
                        reader -> collectHits(reader, parsed, limit));
            }
        } catch (IOException e) {
            throw new ServiceException("検索に失敗しました", e);
        } catch (ParseException e) {
            throw new ApplicationException("検索語の解析に失敗しました: " + query, e);
        }
    }

    /**
     * <p>索引済みファイルに存在する拡張子の一覧を返す（絞り込み UI 用）。</p>
     *
     * @return 拡張子の一覧（小文字・ドットなし、昇順）
     */
    public List<String> listExtensions() {
        try (FSDirectory dir = FSDirectory.open(appPaths.indexDir())) {
            return LuceneReaders.withReader(dir, List.<String>of(), this::collectExtensions);
        } catch (IOException e) {
            throw new ServiceException("拡張子一覧の取得に失敗しました", e);
        }
    }

    private List<String> collectExtensions(DirectoryReader reader) throws IOException {
        Terms terms = MultiTerms.getTerms(reader, LuceneFields.EXTENSION);
        if (terms == null) {
            return List.of();
        }
        List<String> extensions = new ArrayList<>();
        TermsEnum termsEnum = terms.iterator();
        BytesRef term;
        while ((term = termsEnum.next()) != null) {
            String ext = term.utf8ToString();
            if (!ext.isEmpty()) {
                extensions.add(ext);
            }
        }
        Collections.sort(extensions);
        return extensions;
    }

    private List<SearchHit> collectHits(DirectoryReader reader, Query parsed, int limit) throws IOException {
        List<SearchHit> hits = new ArrayList<>();
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs top = searcher.search(parsed, limit);
        StoredFields storedFields = searcher.storedFields();

        QueryScorer scorer = new QueryScorer(parsed, LuceneFields.CONTENT);
        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(HL_PRE, HL_POST), scorer);
        highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, SNIPPET_LENGTH));

        for (ScoreDoc scoreDoc : top.scoreDocs) {
            Document doc = storedFields.document(scoreDoc.doc);
            hits.add(toHit(doc, scoreDoc.score, highlighter));
        }
        return hits;
    }

    /**
     * <p>検索の最大件数を取得する。</p>
     *
     * @return 最大件数
     */
    public int getMaxLimit() {
        return maxLimit;
    }

    /**
     * <p>検索クエリを構築する。拡張子が指定されていれば、スコアに影響しないフィルタとして
     * 絞り込み条件に加える。</p>
     *
     * @param rawQuery  元の検索クエリ
     * @param extension 絞り込む拡張子（小文字・ドットなし）。{@code null} または空文字なら絞り込まない
     * @return 構築されたクエリ
     * @throws ParseException クエリ解析に失敗した場合
     */
    private Query buildQuery(String rawQuery, String extension) throws ParseException {
        String[] fields = {LuceneFields.CONTENT, LuceneFields.NAME};
        Map<String, Float> boosts = Map.of(
                LuceneFields.CONTENT, 1.0f,
                LuceneFields.NAME, 2.0f
        );
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        // ユーザー入力は記号をエスケープし、プレーンな語として扱う
        Query textQuery = parser.parse(QueryParser.escape(rawQuery));
        if (extension == null || extension.isBlank()) {
            return textQuery;
        }
        Term extensionTerm = new Term(LuceneFields.EXTENSION, extension.strip().toLowerCase(Locale.ROOT));
        return new BooleanQuery.Builder()
                .add(textQuery, BooleanClause.Occur.MUST)
                .add(new TermQuery(extensionTerm), BooleanClause.Occur.FILTER)
                .build();
    }

    private SearchHit toHit(Document doc, float score, Highlighter highlighter) {
        String fileName = StringUtils.nullToEmpty(doc.get(LuceneFields.NAME));
        String path = StringUtils.nullToEmpty(doc.get(LuceneFields.PATH));
        String parent = StringUtils.nullToEmpty(doc.get(LuceneFields.PARENT));
        String extension = StringUtils.nullToEmpty(doc.get(LuceneFields.EXTENSION));
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
        return HtmlEscaper.escapeHtml(head);
    }

    /**
     * <p>マーカーを保持したまま HTML エスケープし、最後に {@code <mark>} へ置換する。</p>
     */
    private String decorate(String fragment) {
        return HtmlEscaper.escapeHtml(fragment)
                .replace(HL_PRE, "<mark>")
                .replace(HL_POST, "</mark>");
    }

    private static long numeric(IndexableField field) {
        return (field == null || field.numericValue() == null) ? 0L : field.numericValue().longValue();
    }
}
