package com.example.docsearch.web;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>ヘルスチェック API。</p>
 * <p>2つの用途を提供する。</p>
 *
 * <ul>
 *     <li>{@link #health()}：外部ツール／スクリプトからの疎通確認用 JSON API（{@code GET /api/health}）。</li>
 *     <li>{@link #healthFragment()}：画面の稼働状態バッジ用フラグメント（{@code GET /health-fragment}）。
 *     {@code #healthStatus} が {@code hx-trigger="load, every 10s"} でポーリングする。
 *     このハンドラが応答できている時点で稼働中とみなせるため、状態判定は不要で常に
 *     「起動中」の内容を返す。</li>
 * </ul>
 */
@Controller
public class HealthController {

    /**
     * <p>アプリの稼働状態を返す。</p>
     *
     * @return ステータスとアプリ名を含むマップ
     */
    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "app", "DocSearch"
        );
    }

    /**
     * <p>稼働状態バッジのフラグメントを返す。</p>
     *
     * @return フラグメント名
     */
    @GetMapping("/health-fragment")
    public String healthFragment() {
        return "fragments/health :: badge";
    }
}
