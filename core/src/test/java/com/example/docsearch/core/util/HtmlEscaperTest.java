package com.example.docsearch.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>{@link HtmlEscaper} のテストを行う。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 * <ul>
 *     <li>{@link #escapeHtmlSuccess01} 正常系：HTMLエスケープのテスト（&lt;div&gt; と &lt;/div&gt;）。</li>
 *     <li>{@link #escapeHtmlSuccess02} 正常系：HTMLエスケープのテスト（&amp;）。</li>
 *     <li>{@link #escapeHtmlSuccess03} 正常系：HTMLエスケープのテスト（&quot;）。</li>
 *     <li>{@link #escapeHtmlSuccess04} 正常系：HTMLエスケープのテスト（&#39;）。</li>
 *     <li>{@link #escapeHtmlSuccess05} 正常系：HTMLエスケープのテスト（{@code null}）。</li>
 * </ul>
 */
public class HtmlEscaperTest {

    /**
     * <p>正常系：HTMLエスケープのテスト（&lt;div&gt; と &lt;/div&gt;）。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void escapeHtmlSuccess01() throws Exception {

        //
        // 事前準備
        //
        String input = "<div>Hello Welcome!</div>";
        String excepted = "&lt;div&gt;Hello Welcome!&lt;/div&gt;";

        //
        // 実行・検証
        //
        assertEquals(excepted, HtmlEscaper.escapeHtml(input));
    }

    /**
     * <p>正常系：HTMLエスケープのテスト（&amp;）。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void escapeHtmlSuccess02() throws Exception {

        //
        // 事前準備
        //
        String input = "Love & peace";
        String excepted  = "Love &amp; peace";

        //
        // 実行・検証
        //
        assertEquals(excepted, HtmlEscaper.escapeHtml(input));
    }

    /**
     * <p>正常系：HTMLエスケープのテスト（&quot;）。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void escapeHtmlSuccess03() throws Exception {

        //
        // 事前準備
        //
        String input = "\"Hello\"";
        String excepted = "&quot;Hello&quot;";

        //
        // 実行・検証
        //
        assertEquals(excepted, HtmlEscaper.escapeHtml(input));
    }

    /**
     * <p>正常系：HTMLエスケープのテスト（&#39;）。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void escapeHtmlSuccess04() throws Exception {

        //
        // 事前準備
        //
        String input = "'Hello'";
        String excepted = "&#39;Hello&#39;";

        //
        // 実行・検証
        //
        assertEquals(excepted, HtmlEscaper.escapeHtml(input));
    }

    /**
     * <p>正常系：HTMLエスケープのテスト（null）。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void escapeHtmlSuccess05() throws Exception {

        //
        // 事前準備
        //
        String excepted = "";

        //
        // 実行・検証
        //
        assertEquals(excepted, HtmlEscaper.escapeHtml(null));
    }
}
