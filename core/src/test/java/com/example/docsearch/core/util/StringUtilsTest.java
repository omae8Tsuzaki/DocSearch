package com.example.docsearch.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>{@link StringUtils} のテストを行う。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 * <ul>
 *     <li>{@link #nullToEmptySuccess01()} 正常系：入力値が {@code null} の場合。</li>
 *     <li>{@link #nullToEmptySuccess02()} 正常系：入力値が {@code null} でない場合。</li>
 * </ul>
 */
public class StringUtilsTest {

    /**
     * <p>正常系：入力値が {@code null} の場合。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void nullToEmptySuccess01() throws Exception {

        //
        // 実行・検証
        //
        assertEquals("", StringUtils.nullToEmpty(null));
    }

    /**
     * <p>正常系：入力値が {@code null} でない場合。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void nullToEmptySuccess02() throws Exception {

        //
        // 事前準備
        //
        String input = "test";

        //
        // 実行・検証
        //
        assertEquals("test", StringUtils.nullToEmpty(input));
    }

}
