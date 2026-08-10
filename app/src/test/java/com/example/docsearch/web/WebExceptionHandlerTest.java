package com.example.docsearch.web;

import com.example.docsearch.core.exception.ApplicationException;
import com.example.docsearch.core.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link WebExceptionHandler} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #handleApplicationSuccess01} 正常系：{@link ApplicationException} のメッセージがエラーバナーのモデルに設定されることを確認する。</li>
 *     <li>{@link #handleServiceSuccess01} 正常系：{@link ServiceException} のメッセージがエラーバナーのモデルに設定されることを確認する。</li>
 * </ul>
 */
public class WebExceptionHandlerTest {

    private final WebExceptionHandler handler = new WebExceptionHandler();

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[handleApplication]
        テスト観点：ApplicationException のメッセージがエラーバナーのモデルに設定されることを確認する。
        """)
    public void handleApplicationSuccess01() throws Exception {

        //
        // 事前準備
        //
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = handler.handleApplication(new ApplicationException("検索語の解析に失敗しました"), model);

        //
        // 検証
        //
        assertEquals("fragments/error :: banner", view);
        assertEquals("検索語の解析に失敗しました", model.getAttribute("message"));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[handleService]
        テスト観点：ServiceException のメッセージがエラーバナーのモデルに設定されることを確認する。
        """)
    public void handleServiceSuccess01() throws Exception {

        //
        // 事前準備
        //
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = handler.handleService(new ServiceException("検索に失敗しました"), model);

        //
        // 検証
        //
        assertEquals("fragments/error :: banner", view);
        assertEquals("検索に失敗しました", model.getAttribute("message"));
    }
}
