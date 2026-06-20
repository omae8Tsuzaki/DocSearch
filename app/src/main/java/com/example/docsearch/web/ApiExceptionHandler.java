package com.example.docsearch.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>API 全体の例外を JSON エラーに変換するハンドラ。</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 不正なパス指定など。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() == null ? "不正なリクエストです" : e.getMessage()));
    }
}
