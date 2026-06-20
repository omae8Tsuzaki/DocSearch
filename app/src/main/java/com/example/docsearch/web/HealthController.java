package com.example.docsearch.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>アプリの稼働確認用エンドポイント。</p>
 *
 * <p>Phase 0 の疎通確認に使用する。ブラウザ／クライアントから
 * {@code GET /api/health} を叩き、アプリが起動していることを確認する。</p>
 */
@RestController
public class HealthController {

    /**
     * アプリの稼働状態を返す。
     *
     * @return ステータスとアプリ名を含むマップ
     */
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "app", "DocSearch"
        );
    }
}
