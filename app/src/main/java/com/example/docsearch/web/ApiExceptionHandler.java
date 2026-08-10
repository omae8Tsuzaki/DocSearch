package com.example.docsearch.web;

import java.util.Map;

import com.example.docsearch.core.exception.ApplicationException;
import com.example.docsearch.core.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>API 全体の例外を JSON エラーに変換するハンドラ。</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /// 不正なパス指定など
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() == null ? "不正なリクエストです" : e.getMessage()));
    }

    /// アプリケーション例外
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, String>> handleApplication(ApplicationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /// ビジネスロジック例外
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, String>> handleService(ServiceException e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
}
