package com.example.docsearch.web;

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

    /** 現在の検索対象フォルダ一覧を返す。 */
    @GetMapping
    public Map<String, Object> get() {
        return Map.of("folders", settingsRepository.getFolders());
    }

    /** 検索対象フォルダ一覧を保存し、正規化後の結果を返す。 */
    @PutMapping
    public Map<String, Object> put(@RequestBody FoldersRequest request) {
        List<String> folders = request == null || request.folders() == null
                ? List.of()
                : request.folders();
        return Map.of("folders", settingsRepository.saveFolders(folders));
    }

    /** 保存リクエストのボディ。 */
    public record FoldersRequest(List<String> folders) {
    }
}
