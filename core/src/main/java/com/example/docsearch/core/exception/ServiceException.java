package com.example.docsearch.core.exception;

/// ビジネスロジックで送出される例外クラス。
public class ServiceException extends BaseException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
