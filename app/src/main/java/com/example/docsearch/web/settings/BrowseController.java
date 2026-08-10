package com.example.docsearch.web.settings;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.docsearch.domain.DirectoryBrowser;
import com.example.docsearch.domain.model.DirectoryEntry;

/**
 * <p>フォルダブラウザ（フォルダ追加モーダル）用のディレクトリ一覧表示を担う。</p>
 *
 * <p>{@code path} 未指定ならルート（ドライブ）一覧、指定時はその直下のサブフォルダ
 * 一覧と親パスを表示する。</p>
 */
@Controller
public class BrowseController {

    private final DirectoryBrowser directoryBrowser;

    public BrowseController(DirectoryBrowser directoryBrowser) {
        this.directoryBrowser = directoryBrowser;
    }

    /**
     * <p>ディレクトリ一覧フラグメントを返す。</p>
     *
     * @param path  表示対象パス（省略可。省略時はドライブ一覧）
     * @param model モデル
     * @return フラグメント名
     */
    @GetMapping("/browse")
    public String browse(@RequestParam(required = false) String path, Model model) {
        String current = (path == null || path.isBlank()) ? "" : path;
        List<DirectoryEntry> entries;
        String parent;
        if (current.isEmpty()) {
            entries = directoryBrowser.listRoots();
            parent = "";
        } else {
            entries = directoryBrowser.listChildren(current);
            String parentOf = directoryBrowser.parentOf(current);
            parent = parentOf == null ? "" : parentOf;
        }
        model.addAttribute("current", current);
        model.addAttribute("parent", parent);
        model.addAttribute("entries", entries);
        return "fragments/browse :: entries";
    }
}
