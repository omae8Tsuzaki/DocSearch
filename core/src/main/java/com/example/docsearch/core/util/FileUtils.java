package com.example.docsearch.core.util;

import java.util.Locale;

/**
 * <p>ファイル操作を行うユーティリティクラス。</p>
 */
public class FileUtils {

    private FileUtils() {
        // インスタンス化を防ぐためのコンストラクタ
    }

    /** ファイル名から拡張子（小文字・ドットなし）を返す。無い場合は空文字。 */
    public static String getFileExtensions(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0 && dot < fileName.length() - 1)
                ? fileName.substring(dot + 1).toLowerCase(Locale.ROOT)
                : "";
    }
}
