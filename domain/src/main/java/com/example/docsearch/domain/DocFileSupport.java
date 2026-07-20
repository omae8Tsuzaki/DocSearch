package com.example.docsearch.domain;

import java.nio.file.Path;
import java.util.Set;

import com.example.docsearch.core.util.FileUtils;

/**
 * <p>DocSearch 固有のファイル操作を支援するユーティリティクラス。</p>
 * <p>汎用的なファイル操作などは {@link com.example.docsearch.core.util.FileUtils} を利用する。</p>
 */
public class DocFileSupport {

    private DocFileSupport() {
        // インスタンス化を防ぐためのコンストラクタ
    }

    /**
     * <p>本文抽出の対象拡張子かどうかを判定する。</p>
     *
     * @param file 対象ファイル
     * @param supportedExtensions 対象拡張子の集合（小文字）
     * @return 対象拡張子なら {@code true}、それ以外は {@code false}
     */
    public static boolean isSupportedExtension(Path file, Set<String> supportedExtensions) {
        Path name = file.getFileName();
        if (name == null) {
            return false;
        }
        if (supportedExtensions == null || supportedExtensions.isEmpty()) {
            return false;
        }
        // 拡張子なしの場合は空文字が返り、集合に含まれないため false になる。
        String ext = FileUtils.getFileExtension(name.toString());
        return supportedExtensions.contains(ext);
    }
}
