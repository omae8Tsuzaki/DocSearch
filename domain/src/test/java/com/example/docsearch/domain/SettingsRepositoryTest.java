package com.example.docsearch.domain;

import com.example.docsearch.core.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>{@link SettingsRepository} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #getFoldersSuccess01} 正常系：設定ファイルが存在しない場合、空リストが返却されることを確認する。</li>
 *     <li>{@link #getFoldersSuccess02} 正常系：設定ファイルにフォルダが設定されている場合、その一覧が取得できることを確認する。</li>
 *     <li>{@link #getFoldersError01} 異常系：設定ファイルのパスがディレクトリである場合、ServiceExceptionがスローされることを確認する。</li>
 *     <li>{@link #saveFoldersSuccess01} 正常系：保存したフォルダ一覧が取得できることを確認する。</li>
 *     <li>{@link #saveFoldersSuccess02} 正常系：空白文字や空文字を除去して保存されることを確認する。</li>
 *     <li>{@link #saveFoldersSuccess03} 正常系：重複したフォルダは除去されることを確認する。</li>
 *     <li>{@link #saveFoldersSuccess04} 正常系：引数が null の場合、空リストが返却されることを確認する。</li>
 *     <li>{@link #saveFoldersSuccess05} 正常系：null を含むリストを保存した場合、null は除去されることを確認する。</li>
 *     <li>{@link #saveFoldersError01} 異常系：設定ファイルのパスがディレクトリである場合、ServiceExceptionがスローされることを確認する。</li>
 * </ul>
 */
public class SettingsRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[getFolders]
        テスト観点：設定ファイルが存在しない場合、空リストが返却されることを確認する。
        """)
    public void getFoldersSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);

        List<String> expected = List.of();

        //
        // 実行
        //
        List<String> result = settingsRepository.getFolders();

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[getFolders]
        テスト観点：設定ファイルにフォルダが設定されている場合、その一覧が取得できることを確認する。
        """)
    public void getFoldersSuccess02() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        List<String> expected = List.of("C:\\folder1", "C:\\folder2");
        Files.write(appPaths.settingsFile(), expected, StandardCharsets.UTF_8);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);

        //
        // 実行
        //
        List<String> result = settingsRepository.getFolders();

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        異常系
        対象メソッド：[getFolders]
        テスト観点：設定ファイルのパスがディレクトリである場合、ServiceExceptionがスローされることを確認する。
        """)
    public void getFoldersError01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        Files.createDirectories(appPaths.settingsFile());
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);

        //
        // 実行 & 検証
        //
        assertThrows(ServiceException.class, settingsRepository::getFolders);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[saveFolders]
        テスト観点：保存したフォルダ一覧が取得できることを確認する。
        """)
    public void saveFoldersSuccess01() throws Exception {
        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        List<String> expected = List.of("C:\\folder1", "C:\\folder2");

        //
        // 事前準備
        //
        List<String> result = settingsRepository.saveFolders(List.of("C:\\folder1", "C:\\folder2"));

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[saveFolders]
        テスト観点：空白文字や空文字を除去して保存されることを確認する。
        """)
    public void saveFoldersSuccess02() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        List<String> expected = List.of("C:\\folder1");

        //
        // 事前準備
        //
        List<String> result = settingsRepository.saveFolders(List.of("C:\\folder1", " ", ""));

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[saveFolders]
        テスト観点：重複したフォルダは除去されることを確認する。
        """)
    public void saveFoldersSuccess03() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        List<String> expected = List.of("C:\\folder1");

        //
        // 事前準備
        //
        List<String> result = settingsRepository.saveFolders(List.of("C:\\folder1", "C:\\folder1"));

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[saveFolders]
        テスト観点：引数が null の場合、空リストが返却されることを確認する。
        """)
    public void saveFoldersSuccess04() throws Exception {
        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        List<String> expected = List.of();

        //
        // 事前準備
        //
        List<String> result = settingsRepository.saveFolders(null);

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[saveFolders]
        テスト観点：null を含むリストを保存した場合、null は除去されることを確認する。
        """)
    public void saveFoldersSuccess05() throws Exception {
        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        List<String> expected = List.of("C:\\folder1");

        //
        // 事前準備
        //
        List<String> result = settingsRepository.saveFolders(Arrays.asList("C:\\folder1", null));

        //
        // 検証
        //
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("""
        異常系
        対象メソッド：[saveFolders]
        テスト観点：設定ファイルのパスがディレクトリである場合、ServiceExceptionがスローされることを確認する。
        """)
    public void saveFoldersError01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        Files.createDirectories(appPaths.settingsFile());
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);

        //
        // 実行 & 検証
        //
        assertThrows(ServiceException.class, () -> settingsRepository.saveFolders(List.of("C:\\folder1")));
    }

}
