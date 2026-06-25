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
     * @param query 検索クエリ
     * @return 検索結果
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam("q") String query) {
        List<SearchHit> hits = searchService.search(query);
        return Map.of(
                "query", query == null ? "" : query,
                "total", hits.size(),
                "limit", FullTextSearchService.DEFAULT_LIMIT,
                "hits", hits
        );
    }
}
