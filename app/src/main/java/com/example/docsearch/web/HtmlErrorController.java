package com.example.docsearch.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>ブラウザからの通常ページ遷移で例外・404 が発生した際に {@code error.html} を表示するハンドラ。</p>
 *
 * <p>{@link BasicErrorController} を継承し、HTML を要求するリクエストのみ挙動を上書きする。
 * {@code /api/**} からの JSON レスポンス（{@link ApiExceptionHandler} が扱う）には影響しない。</p>
 */
@Controller
public class HtmlErrorController extends BasicErrorController {

    public HtmlErrorController(ErrorAttributes errorAttributes, WebProperties webProperties) {
        super(errorAttributes, webProperties.getError());
    }

    @Override
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        response.setStatus(status.value());
        return new ModelAndView("forward:/error.html");
    }
}
