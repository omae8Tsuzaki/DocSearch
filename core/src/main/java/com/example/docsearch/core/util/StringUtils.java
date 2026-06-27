package com.example.docsearch.core.util;

/**
 * <p>文字列操作のユーティリティクラス。</p>
 */
public class StringUtils {

    private StringUtils() {
    }

    /**
     * <p>{@code null} 値を空文字列に変換する。</p>
     *
     * @param s 変換対象の文字列
     * @return {@code null} の場合は空文字列、それ以外の場合は元の文字列
     */
    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
