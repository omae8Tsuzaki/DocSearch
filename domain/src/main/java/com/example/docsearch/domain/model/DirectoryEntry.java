package com.example.docsearch.domain.model;

/**
 * <p>ディレクトリブラウザで表示する1エントリ（ドライブまたはサブフォルダ）。</p>
 *
 * @param name 表示名（ドライブなら {@code "C:\\"}、フォルダならフォルダ名）
 * @param path 絶対パス
 */
public record DirectoryEntry(
        String name,
        String path
) {
}
