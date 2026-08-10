package com.example.docsearch.web.search;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.docsearch.domain.SettingsRepository;
import com.example.docsearch.domain.model.IndexStatus;
import com.example.docsearch.domain.service.FullTextSearchService;
import com.example.docsearch.domain.service.LuceneIndexService;

/**
 * <p>トップページの表示と、索引（インデックス）の作成・状態表示を担う。</p>
 *
 * <p>索引状態は {@code #indexStatus} が {@code hx-trigger="every 2s"} でポーリングし、
 * 併せて拡張子の絞り込み候補（{@code #extFilter}）を out-of-band swap で更新する。</p>
 */
@Controller
public class IndexController {

    private final LuceneIndexService indexService;
    private final SettingsRepository settingsRepository;
    private final FullTextSearchService searchService;

    public IndexController(LuceneIndexService indexService, SettingsRepository settingsRepository,
                            FullTextSearchService searchService) {
        this.indexService = indexService;
        this.settingsRepository = settingsRepository;
        this.searchService = searchService;
    }

    /**
     * <p>トップページを表示する。</p>
     *
     * @param model モデル
     * @return ビュー名
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("folders", settingsRepository.getFolders());
        addStatusAttributes(model, null, null);
        model.addAttribute("blank", true);
        model.addAttribute("hits", List.of());
        model.addAttribute("total", 0);
        model.addAttribute("limit", searchService.getMaxLimit());
        return "index";
    }

    /**
     * <p>再索引を開始する。対象フォルダ未設定・既に実行中の場合はエラーメッセージを状態表示に含める。</p>
     *
     * @param model モデル
     * @return フラグメント名
     */
    @PostMapping("/index/reindex")
    public String reindex(Model model) {
        List<String> folders = settingsRepository.getFolders();
        String error = null;
        if (folders.isEmpty()) {
            error = "検索対象フォルダが未設定です";
        } else if (!indexService.startReindex(folders)) {
            error = "索引作成が既に実行中です";
        }
        addStatusAttributes(model, null, error);
        return "fragments/index-status :: pollResponse";
    }

    /**
     * <p>索引状態のポーリング用。拡張子の絞り込み候補もあわせて更新する。</p>
     *
     * @param ext   現在選択中の拡張子（選択状態を維持するために送り返す）
     * @param model モデル
     * @return フラグメント名
     */
    @GetMapping("/index/status-fragment")
    public String statusFragment(@RequestParam(value = "ext", required = false) String ext, Model model) {
        addStatusAttributes(model, ext, null);
        return "fragments/index-status :: pollResponse";
    }

    private void addStatusAttributes(Model model, String selectedExt, String reindexError) {
        IndexStatus status = indexService.status();
        model.addAttribute("status", status);
        model.addAttribute("lastIndexedText", status.lastIndexedEpochMs() > 0
                ? "最終索引 " + ViewFormatters.formatDateTime(status.lastIndexedEpochMs())
                : "未索引");
        model.addAttribute("extensions", searchService.listExtensions());
        model.addAttribute("selectedExt", selectedExt == null ? "" : selectedExt);
        model.addAttribute("reindexError", reindexError);
    }
}
