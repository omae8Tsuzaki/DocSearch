package com.example.docsearch.web;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link HealthController} の単体テスト。</p>
 */
public class HealthControllerTest {

    HealthController controller = new HealthController();

    /**
     * <p>health メソッドの正常系テスト。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void healthSuccess01() throws Exception {

        //
        // 実行
        //
        Map<String, String> result = controller.health();

        //
        // 検証
        //
        assertEquals("UP", result.get("status"));
        assertEquals("DocSearch", result.get("app"));
    }
}