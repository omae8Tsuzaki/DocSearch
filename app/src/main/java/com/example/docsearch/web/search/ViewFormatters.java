package com.example.docsearch.web.search;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * <p>Thymeleaf テンプレートに渡す値の表示用フォーマットを行うユーティリティクラス。</p>
 *
 * <p>画面表示専用の整形処理のため {@code app} 層に置く。{@code domain} / {@code core} には持たせない。</p>
 */
public final class ViewFormatters {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN).withZone(ZoneId.systemDefault());

    private ViewFormatters() {
        // インスタンス化を防ぐためのコンストラクタ
    }

    /**
     * <p>エポックミリ秒を {@code yyyy-MM-dd HH:mm} 形式に整形する。</p>
     *
     * @param epochMs エポックミリ秒（0以下は未設定とみなす）
     * @return 整形後の文字列。未設定なら {@code "-"}
     */
    public static String formatDateTime(long epochMs) {
        if (epochMs <= 0) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMs));
    }

    /**
     * <p>バイト数を読みやすい単位（KB/MB/GB/TB）に整形する。</p>
     *
     * @param bytes バイト数
     * @return 整形後の文字列
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes / 1024.0;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format(Locale.JAPAN, "%.1f %s", value, units[unitIndex]);
    }
}
