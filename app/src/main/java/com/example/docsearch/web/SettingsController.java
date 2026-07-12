package com.example.docsearch.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.docsearch.domain.SettingsRepository;

/**
 * <p>検索対象フォルダ設定の取得・保存 API。</p>
 */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsRepository settingsRepository;

    public SettingsController(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * <p>現在の検索対象フォルダ一覧を返す。</p>
     *
     * @return フォルダパスのリストを持つ JSON オブジェクト
     */
    @GetMapping
    public Map<String, Object> get() {
        return Map.of("folders", settingsRepository.getFolders());
    }

    /**
     * <p>検索対象フォルダ一覧を保存し、正規化後の結果を返す。</p>
     *
     * @param request 保存するフォルダパスのリスト
     * @return 正規化後のフォルダパスのリストを持つ JSON オブジェクト
     */
    @PutMapping
    public Map<String, Object> put(@RequestBody FoldersRequest request) {
        List<String> folders = request == null ? List.of() : request.getFolders();
        return Map.of("folders", settingsRepository.saveFolders(folders));
    }

    /**
     * <p>保存リクエストのボディ。</p>
     */
    public static class FoldersRequest {

        private List<String> folders = new ArrayList<>();

        public void setFolders(List<String> folders) {
            this.folders = folders == null ? new ArrayList<>() : new ArrayList<>(folders);
        }

        public List<String> getFolders() {
            return Collections.unmodifiableList(new ArrayList<>(folders));
        }
    }
}
