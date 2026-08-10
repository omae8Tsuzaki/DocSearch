package com.example.docsearch.web.settings;

import java.nio.file.Path;
import java.util.List;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.SettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link SettingsController} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #addSuccess01} 正常系：フォルダを追加すると、更新後の一覧がモデルに設定されることを確認する。</li>
 *     <li>{@link #removeSuccess01} 正常系：フォルダを削除すると、更新後の一覧がモデルに設定されることを確認する。</li>
 * </ul>
 */
public class SettingsControllerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[add]
        テスト観点：フォルダを追加すると、更新後の一覧がモデルに設定されることを確認する。
        """)
    public void addSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        SettingsController controller = new SettingsController(settingsRepository);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.add("C:\\Users\\User\\Documents", model);

        //
        // 検証
        //
        assertEquals("fragments/folders :: section", view);
        assertEquals(List.of("C:\\Users\\User\\Documents"), model.getAttribute("folders"));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[remove]
        テスト観点：フォルダを削除すると、更新後の一覧がモデルに設定されることを確認する。
        """)
    public void removeSuccess01() throws Exception {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        settingsRepository.saveFolders(List.of("C:\\folder1", "C:\\folder2"));
        SettingsController controller = new SettingsController(settingsRepository);
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.remove("C:\\folder1", model);

        //
        // 検証
        //
        assertEquals("fragments/folders :: section", view);
        assertEquals(List.of("C:\\folder2"), model.getAttribute("folders"));
    }
}
