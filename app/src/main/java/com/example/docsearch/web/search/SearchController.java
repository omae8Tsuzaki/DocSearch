package com.example.docsearch.web.search;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.docsearch.domain.model.SearchHit;
import com.example.docsearch.domain.service.FullTextSearchService;

/**
 * <p>全文検索。Lucene 索引に対して本文・ファイル名を横断検索する。</p>
 */
@Controller
public class SearchController {

    private final FullTextSearchService searchService;

    public SearchController(FullTextSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * <p>クエリ文字列を受け取り、検索結果フラグメントを返す。</p>
     *
     * @param query     検索クエリ
     * @param extension 絞り込む拡張子（省略可、小文字・ドットなし）
     * @param model     モデル
     * @return フラグメント名
     */
    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String query,
                          @RequestParam(value = "ext", required = false) String extension,
                          Model model) {
        boolean blank = query == null || query.isBlank();
        model.addAttribute("blank", blank);
        model.addAttribute("limit", searchService.getMaxLimit());
        if (blank) {
            model.addAttribute("hits", List.of());
            model.addAttribute("total", 0);
            return "fragments/search-results :: results";
        }
        List<SearchHit> hits = searchService.search(query, searchService.getMaxLimit(), extension);
        model.addAttribute("hits", hits.stream().map(SearchHitView::from).toList());
        model.addAttribute("total", hits.size());
        return "fragments/search-results :: results";
    }
}
