package com.example.docsearch.web.settings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.example.docsearch.domain.DirectoryBrowser;
import com.example.docsearch.domain.model.DirectoryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link BrowseController} のテストクラス。</p>
 *
 * <p>以下の観点でテストを行う。</p>
 *
 * <ul>
 *     <li>{@link #browseSuccess01} 正常系：パス未指定の場合、ドライブ一覧が返却されることを確認する。</li>
 *     <li>{@link #browseSuccess02} 正常系：パス指定時、そのサブフォルダ一覧と親パスが返却されることを確認する。</li>
 * </ul>
 */
public class BrowseControllerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[browse]
        テスト観点：パス未指定の場合、ドライブ一覧が返却されることを確認する。
        """)
    public void browseSuccess01() throws Exception {

        //
        // 事前準備
        //
        BrowseController controller = new BrowseController(new DirectoryBrowser());
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.browse(null, model);

        //
        // 検証
        //
        assertEquals("fragments/browse :: entries", view);
        assertEquals("", model.getAttribute("current"));
        assertEquals("", model.getAttribute("parent"));
    }

    @Test
    @DisplayName("""
        正常系
        対象メソッド：[browse]
        テスト観点：パス指定時、そのサブフォルダ一覧と親パスが返却されることを確認する。
        """)
    public void browseSuccess02() throws Exception {

        //
        // 事前準備
        //
        Files.createDirectories(tempDir.resolve("child"));
        BrowseController controller = new BrowseController(new DirectoryBrowser());
        Model model = new ExtendedModelMap();

        //
        // 実行
        //
        String view = controller.browse(tempDir.toString(), model);

        //
        // 検証
        //
        assertEquals("fragments/browse :: entries", view);
        assertEquals(tempDir.toString(), model.getAttribute("current"));
        @SuppressWarnings("unchecked")
        List<DirectoryEntry> entries = (List<DirectoryEntry>) model.getAttribute("entries");
        assertEquals(1, entries.size());
        assertEquals("child", entries.get(0).name());
    }
}
