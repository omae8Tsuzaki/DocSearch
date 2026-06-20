package com.example.docsearch.domain;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

/**
 * <p>アプリのデータ保存先（設定ファイルや Lucene 索引）を解決するクラス。</p>
 *
 * <p>Windows では {@code %LOCALAPPDATA%\DocSearch} を、取得できない環境では
 * {@code <ユーザーホーム>/.docsearch} を基準ディレクトリとする。</p>
 */
@Component
public final class AppPaths {

    private final Path baseDir;

    /** 既定の保存先を用いる（Spring はこのコンストラクタを使用）。 */
    public AppPaths() {
        this(resolveDefaultBaseDir());
    }

    /**
     * 任意の保存先を指定する（主にテスト用）。
     *
     * @param baseDir 基準ディレクトリ
     */
    public AppPaths(Path baseDir) {
        this.baseDir = baseDir;
    }

    private static Path resolveDefaultBaseDir() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Path.of(localAppData, "DocSearch");
        }
        return Path.of(System.getProperty("user.home"), ".docsearch");
    }

    /**
     * 基準ディレクトリを返す（存在しなければ作成する）。
     *
     * @return 基準ディレクトリ
     */
    public Path baseDir() {
        return ensure(baseDir);
    }

    /**
     * 検索対象フォルダ設定ファイルのパスを返す。
     *
     * @return {@code folders.txt} の絶対パス
     */
    public Path settingsFile() {
        return baseDir().resolve("folders.txt");
    }

    /**
     * Lucene 索引を格納するディレクトリを返す（存在しなければ作成する）。
     *
     * @return 索引ディレクトリ
     */
    public Path indexDir() {
        return ensure(baseDir().resolve("index"));
    }

    /**
     * 最終索引時刻を記録するメタファイルのパスを返す。
     *
     * @return {@code index.meta} の絶対パス
     */
    public Path indexMetaFile() {
        return baseDir().resolve("index.meta");
    }

    private static Path ensure(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("ディレクトリの作成に失敗しました: " + dir, e);
        }
        return dir;
    }
}
