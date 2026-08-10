package com.example.docsearch.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.docsearch.domain.service.FullTextSearchService;
import com.example.docsearch.domain.model.SearchHit;

/**
 * <p>全文検索 API。Lucene 索引に対して本文・ファイル名を横断検索する。</p>
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final FullTextSearchService searchService;

    public SearchController(FullTextSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * <p>クエリ文字列を受け取り、検索してヒットしたドキュメントのリストを返す。</p>
     *
     * @param query     検索クエリ
     * @param extension 絞り込む拡張子（省略可、小文字・ドットなし）
     * @return 検索結果
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam("q") String query,
                                       @RequestParam(value = "ext", required = false) String extension) {
        List<SearchHit> hits = searchService.search(query, searchService.getMaxLimit(), extension);
        return Map.of(
                "query", query == null ? "" : query,
                "total", hits.size(),
                "limit", searchService.getMaxLimit(),
                "hits", hits
        );
    }

    /**
     * <p>絞り込み UI 用に、索引済みファイルに存在する拡張子の一覧を返す。</p>
     *
     * @return 拡張子の一覧（小文字・ドットなし、昇順）
     */
    @GetMapping("/extensions")
    public Map<String, Object> extensions() {
        return Map.of("extensions", searchService.listExtensions());
    }
}
