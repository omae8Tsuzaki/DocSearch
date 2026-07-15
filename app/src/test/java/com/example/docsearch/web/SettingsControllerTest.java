package com.example.docsearch.web;

import com.example.docsearch.domain.AppPaths;
import com.example.docsearch.domain.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link SettingsController} のテストクラス。</p>
 */
public class SettingsControllerTest {

    @TempDir
    Path tempDir;

    @Test
    public void getSuccess01() {

        //
        // 事前準備
        //
        AppPaths appPaths = new AppPaths(tempDir);
        SettingsRepository settingsRepository = new SettingsRepository(appPaths);
        settingsRepository.saveFolders(List.of("C:\\Users\\User\\Documents"));
        SettingsController controller = new SettingsController(settingsRepository);


        //
        // 実行
        //
        Map<String, Object> result =  controller.get();

        //
        // 検証
        //
        assertEquals(List.of("C:\\Users\\User\\Documents"), result.get("folders"));
    }
}