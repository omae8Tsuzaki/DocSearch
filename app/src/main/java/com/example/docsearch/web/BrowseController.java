package com.example.docsearch.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.docsearch.domain.DirectoryBrowser;

/**
 * <p>ディレクトリブラウザ用 API。</p>
 *
 * <p>{@code path} 未指定ならルート（ドライブ）一覧、指定時はその直下のサブフォルダ
 * 一覧と親パスを返す。</p>
 */
@RestController
@RequestMapping("/api/browse")
public class BrowseController {

    private final DirectoryBrowser directoryBrowser;

    public BrowseController(DirectoryBrowser directoryBrowser) {
        this.directoryBrowser = directoryBrowser;
    }

    @GetMapping
    public Map<String, Object> browse(@RequestParam(required = false) String path) {
        Map<String, Object> result = new HashMap<>();
        if (path == null || path.isBlank()) {
            result.put("current", "");
            result.put("parent", "");
            result.put("entries", directoryBrowser.listRoots());
            return result;
        }
        String parent = directoryBrowser.parentOf(path);
        result.put("current", path);
        result.put("parent", parent == null ? "" : parent);
        result.put("entries", directoryBrowser.listChildren(path));
        return result;
    }
}
