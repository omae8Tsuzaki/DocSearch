package com.example.docsearch.web.settings;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.docsearch.domain.SettingsRepository;

/**
 * <p>検索対象フォルダの追加・削除を担う。</p>
 *
 * <p>いずれも更新後のフォルダ一覧フラグメント（{@code #folderSection}）を返す。</p>
 */
@Controller
public class SettingsController {

    private final SettingsRepository settingsRepository;

    public SettingsController(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * <p>検索対象フォルダを1件追加する。</p>
     *
     * @param path  追加するフォルダの絶対パス
     * @param model モデル
     * @return フラグメント名
     */
    @PostMapping("/folders")
    public String add(@RequestParam("path") String path, Model model) {
        List<String> folders = new ArrayList<>(settingsRepository.getFolders());
        folders.add(path);
        model.addAttribute("folders", settingsRepository.saveFolders(folders));
        return "fragments/folders :: section";
    }

    /**
     * <p>検索対象フォルダを1件削除する。</p>
     *
     * @param path  削除するフォルダの絶対パス
     * @param model モデル
     * @return フラグメント名
     */
    @DeleteMapping("/folders")
    public String remove(@RequestParam("path") String path, Model model) {
        List<String> folders = new ArrayList<>(settingsRepository.getFolders());
        folders.remove(path);
        model.addAttribute("folders", settingsRepository.saveFolders(folders));
        return "fragments/folders :: section";
    }
}
