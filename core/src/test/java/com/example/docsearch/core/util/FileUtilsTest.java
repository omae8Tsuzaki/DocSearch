package com.example.docsearch.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>{@link FileUtils} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 * <ul>
 *     <li>{@link #getFileExtensionSuccess01} 正常系：拡張子が返されることを確認する。</li>
 *     <li>{@link #getFileExtensionSuccess02} 正常系：ファイル名以外を入力した場合、空文字を返すことを確認する。</li>
 * </ul>
 */
public class FileUtilsTest {

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[getFileExtension]
        テスト観点：拡張子が返されることを確認する。
        """)
    public void getFileExtensionSuccess01() throws Exception {
        //
        // 事前準備
        //
        String input = "test.txt";

        //
        // 実行
        //
        var result = FileUtils.getFileExtension(input);

        //
        // 検証
        //
        assertEquals("txt", result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[getFileExtension]
        テスト観点：ファイル名以外を入力した場合、空文字を返すことを確認する。
        """)
    public void getFileExtensionSuccess02() throws Exception {
        //
        // 事前準備
        //
        String input = "Not File Name";

        //
        // 実行
        //
        var result = FileUtils.getFileExtension(input);

        //
        // 検証
        //
        assertEquals("", result);
    }

}
