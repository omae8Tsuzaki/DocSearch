package com.example.docsearch.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>{@link DocFileSupport} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 * <ul>
 *     <li>{@link #isSupportedExtensionSuccess01} 正常系：指定したファイルの拡張子が拡張子一覧に含まれる場合、{@code true} が返ることを確認する。</li>
 *     <li>{@link #isSupportedExtensionSuccess02} 正常系：対象の拡張子一覧が {@code null} の場合、{@code false} が返ることを確認する。</li>
 *     <li>{@link #isSupportedExtensionSuccess03} 正常系：対象の拡張子一覧が空の場合、{@code false} が返ることを確認する。</li>
 *     <li>{@link #isSupportedExtensionSuccess04} 正常系：ファイル名要素を持たないパス（ルート）で getFileName() が {@code null} の場合、{@code false} が返ることを確認する。</li>
 * </ul>
 */
public class DocFileSupportTest {

    @Test
    @DisplayName("""
        正常系
        対象メソッド[isSupportedExtension]
        テスト観点：指定したファイルの拡張子が拡張子一覧に含まれる場合、true が返ることを確認する。
        """)
    public void isSupportedExtensionSuccess01() throws Exception {
        //
        // 事前準備
        //
        Path file = Path.of("sample.txt");
        Set<String> extend = new HashSet<>();
        extend.add("txt");

        //
        // 実行・検証
        //
        assertTrue(DocFileSupport.isSupportedExtension(file, extend));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド[isSupportedExtension]
        テスト観点：対象の拡張子一覧が null の場合、false が返ることを確認する。
        """)
    public void isSupportedExtensionSuccess02() throws Exception {
        //
        // 事前準備
        //
        Path file = Path.of("sample.txt");

        //
        // 実行・検証
        //
        assertFalse(DocFileSupport.isSupportedExtension(file, null));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド[isSupportedExtension]
        テスト観点：対象の拡張子一覧が空の場合、false が返ることを確認する。
        """)
    public void isSupportedExtensionSuccess03() throws Exception {
        //
        // 事前準備
        //
        Path file = Path.of("sample.txt");
        Set<String> extend = new HashSet<>();

        //
        // 実行・検証
        //
        assertFalse(DocFileSupport.isSupportedExtension(file, extend));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド[isSupportedExtension]
        テスト観点：ファイル名要素を持たないパス（ルート）で getFileName() が null の場合、false が返ることを確認する。
        """)
    public void isSupportedExtensionSuccess04() throws Exception {
        //
        // 事前準備
        //
        // ルートパスは getFileName() が null を返す（例: C:\）。
        Path file = FileSystems.getDefault().getRootDirectories().iterator().next();
        Set<String> extend = new HashSet<>();
        extend.add("txt");

        //
        // 実行・検証
        //
        assertFalse(DocFileSupport.isSupportedExtension(file, extend));
    }
}
