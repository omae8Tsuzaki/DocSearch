package com.example.docsearch.core.exception;

/// プレゼンテーション層に返す例外クラス。
public class ApplicationException extends BaseException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
