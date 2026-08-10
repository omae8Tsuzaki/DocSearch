package com.example.docsearch.web.search;

import java.nio.file.Path;
import java.util.List;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.DomainConfig;
import com.example.docsearch.domain.SettingsRepository;
import com.example.docsearch.domain.TextExtractor;
import com.example.docsearch.domain.model.IndexStatus;
import com.example.docsearch.domain.service.FullTextSearchService;
import com.example.docsearch.domain.service.LuceneIndexService;
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

/**
 * <p>{@link IndexController} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #indexSuccess01} 正常系：トップページ表示時、保存済みフォルダ一覧がモデルに設定されることを確認する。</li>
 *     <li>{@link #reindexError01} 異常系：検索対象フォルダが未設定の場合、再索引を開始せずエラーメッセージを設定することを確認する。</li>
 *     <li>{@link #statusFragmentSuccess01} 正常系：選択中の拡張子がモデルにそのまま引き継がれることを確認する。</li>
 * </ul>
 */
@SpringBootTest(properties = "spring.config.import=classpath:domain-config.yml")
@ContextConfiguration(classes = {DomainConfig.class, LuceneAnalyzerConfig.class})
public class IndexControllerTest {

    @Autowired
    private DomainConfig config;

    @Autowired
    private Analyzer analyzer;

    @TempDir
    Path tempDir;

    private IndexController newController(AppPaths appPaths) {
        LuceneIndexService indexService = new LuceneIndexService(appPaths, new TextExtractor(config), config, analyzer);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        FullTextSearchService searchService = new FullTextSearchService(appPaths, config, analyzer);
        return new IndexController(indexService, settingsRepository, searchService);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[index]
        テスト観点：トップページ表示時、保存済みフォルダ一覧がモデルに設定されることを確認する。
        """)
    public void indexSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        new SettingsRepository(appPaths).saveFolders(List.of("C:\\folder1"));
        IndexController controller = newController(appPaths);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.index(model);

        //
        // 検証
        //
        assertEquals("index", view);
        assertEquals(List.of("C:\\folder1"), model.getAttribute("folders"));
        assertEquals(true, model.getAttribute("blank"));
    }

    @Test
    @DisplayName("""
        異常系
        対象メソッド：[reindex]
        テスト観点：検索対象フォルダが未設定の場合、再索引を開始せずエラーメッセージを設定することを確認する。
        """)
    public void reindexError01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        IndexController controller = newController(appPaths);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.reindex(model);

        //
        // 検証
        //
        assertEquals("fragments/index-status :: pollResponse", view);
        assertEquals("検索対象フォルダが未設定です", model.getAttribute("reindexError"));
        assertEquals(false, ((IndexStatus) model.getAttribute("status")).indexing());
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[statusFragment]
        テスト観点：選択中の拡張子がモデルにそのまま引き継がれることを確認する。
        """)
    public void statusFragmentSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        IndexController controller = newController(appPaths);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.statusFragment("pdf", model);

        //
        // 検証
        //
        assertEquals("fragments/index-status :: pollResponse", view);
        assertEquals("pdf", model.getAttribute("selectedExt"));
    }
}
