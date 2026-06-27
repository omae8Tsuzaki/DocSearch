package com.example.docsearch.core.util;

/**
 * <p>HTML特殊文字をエスケープするユーティリティクラス。</p>
 */
public class HtmlEscaper {

    private HtmlEscaper() {
    }

    /**
     * <p>HTML特殊文字（&amp; &lt; &gt; " '）をエスケープする。</p>
     *
     * @param s 対象文字列
     * @return エスケープ済み文字列
     */
    public static String escapeHtml(String s) {
        // null の場合は空文字を返す
        if (s == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
