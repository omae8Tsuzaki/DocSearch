package com.example.docsearch.core.exception;

/**
 * <p>例外の基底クラス。</p>
 */
public abstract class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
