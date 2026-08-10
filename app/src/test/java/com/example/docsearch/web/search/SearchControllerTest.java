package com.example.docsearch.web.search;

import java.nio.file.Path;
import java.util.List;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.DomainConfig;
import com.example.docsearch.domain.service.FullTextSearchService;
import com.example.docsearch.domain.service.lucene.LuceneAnalyzerConfig;
import org.apache.lucene.analysis.Analyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>{@link SearchController} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #searchSuccess01} 正常系：検索語が空の場合、検索を行わず {@code blank=true} のフラグメントを返すことを確認する。</li>
 *     <li>{@link #searchSuccess02} 正常系：未索引の状態で検索した場合、ヒット0件のフラグメントを返すことを確認する。</li>
 * </ul>
 */
@SpringBootTest(properties = "spring.config.import=classpath:domain-config.yml")
@ContextConfiguration(classes = {DomainConfig.class, LuceneAnalyzerConfig.class})
public class SearchControllerTest {

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
        テスト観点：検索語が空の場合、検索を行わず blank=true のフラグメントを返すことを確認する。
        """)
    public void searchSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        FullTextSearchService searchService = new FullTextSearchService(appPaths, config, analyzer);
        SearchController controller = new SearchController(searchService);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.search(" ", null, model);

        //
        // 検証
        //
        assertEquals("fragments/search-results :: results", view);
        assertEquals(true, model.getAttribute("blank"));
        assertEquals(List.of(), model.getAttribute("hits"));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[search]
        テスト観点：未索引の状態で検索した場合、ヒット0件のフラグメントを返すことを確認する。
        """)
    public void searchSuccess02() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        FullTextSearchService searchService = new FullTextSearchService(appPaths, config, analyzer);
        SearchController controller = new SearchController(searchService);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.search("テスト", null, model);

        //
        // 検証
        //
        assertEquals("fragments/search-results :: results", view);
        assertEquals(false, model.getAttribute("blank"));
        assertEquals(0, model.getAttribute("total"));
        assertTrue(((List<?>) model.getAttribute("hits")).isEmpty());
    }
}
