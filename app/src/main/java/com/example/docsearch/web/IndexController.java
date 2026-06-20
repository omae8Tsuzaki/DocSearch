package com.example.docsearch.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.docsearch.domain.service.LuceneIndexService;
import com.example.docsearch.domain.SettingsRepository;
import com.example.docsearch.domain.model.IndexStatus;

/**
 * <p>索引の作成（再索引）と状態取得 API。</p>
 *
 * <p>再索引はバックグラウンドで実行されるため、本 API はトリガのみを行い、進捗は
 * {@code GET /api/index/status} をポーリングして確認する。</p>
 */
@RestController
@RequestMapping("/api/index")
public class IndexController {

    private final LuceneIndexService indexService;
    private final SettingsRepository settingsRepository;

    public IndexController(LuceneIndexService indexService, SettingsRepository settingsRepository) {
        this.indexService = indexService;
        this.settingsRepository = settingsRepository;
    }

    /** 再索引を開始する。 */
    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindex() {
        List<String> folders = settingsRepository.getFolders();
        if (folders.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "検索対象フォルダが未設定です"));
        }
        boolean started = indexService.startReindex(folders);
        if (!started) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "索引作成が既に実行中です"));
        }
        return ResponseEntity.accepted().body(Map.of("started", true));
    }

    /** 索引の現在状態を返す。 */
    @GetMapping("/status")
    public IndexStatus status() {
        return indexService.status();
    }
}
