package com.example.docsearch.web;

import com.example.docsearch.core.exception.ApplicationException;
import com.example.docsearch.core.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>画面全体の例外をエラーバナー用フラグメントに変換するハンドラ。</p>
 *
 * <p>htmx はデフォルトでは 4xx/5xx レスポンスをスワップしないが、{@code index.html} で
 * {@code htmx.config.responseHandling} を上書きしてスワップを許可しているため、
 * ここで返すフラグメントはリクエスト元の {@code hx-target} にそのまま差し込まれる。</p>
 */
@ControllerAdvice
public class WebExceptionHandler {

    /// 不正なパス指定など
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage() == null ? "不正なリクエストです" : e.getMessage());
        return "fragments/error :: banner";
    }

    /// アプリケーション例外（利用者の入力起因）
    @ExceptionHandler(ApplicationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleApplication(ApplicationException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "fragments/error :: banner";
    }

    /// サービス例外（サーバー起因の想定外失敗）
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleService(ServiceException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "fragments/error :: banner";
    }
}
