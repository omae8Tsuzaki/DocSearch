package com.example.docsearch.web;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link HealthController} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #healthSuccess01} 正常系：疎通確認用 JSON API がステータスとアプリ名を返却することを確認する。</li>
 *     <li>{@link #healthFragmentSuccess01} 正常系：稼働状態バッジのフラグメント名が返却されることを確認する。</li>
 * </ul>
 */
public class HealthControllerTest {

    private final HealthController controller = new HealthController();

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[health]
        テスト観点：疎通確認用 JSON API がステータスとアプリ名を返却することを確認する。
        """)
    public void healthSuccess01() throws Exception {

        //
        // 実行
        //
        Map<String, String> result = controller.health();

        //
        // 検証
        //
        assertEquals(Map.of("status", "UP", "app", "DocSearch"), result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[healthFragment]
        テスト観点：稼働状態バッジのフラグメント名が返却されることを確認する。
        """)
    public void healthFragmentSuccess01() throws Exception {

        //
        // 実行
        //
        String result = controller.healthFragment();

        //
        // 検証
        //
        assertEquals("fragments/health :: badge", result);
    }
}
