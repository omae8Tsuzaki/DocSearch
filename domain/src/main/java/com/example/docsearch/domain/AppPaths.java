package com.example.docsearch.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.example.docsearch.core.exception.ServiceException;
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
     * <p>任意の保存先を指定する（主にテスト用）。</p>
     *
     * @param baseDir 基準ディレクトリ
     */
    public AppPaths(Path baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * 既定の基準ディレクトリを解決して返す（ディレクトリ作成は行わない）。
     *
     * <p>ログ出力先など、Spring の DI が利用できない起動初期段階で
     * 同じ保存先を参照したい場合に用いる。</p>
     *
     * @return 既定の基準ディレクトリ
     */
    public static Path defaultBaseDir() {
        return resolveDefaultBaseDir();
    }

    private static Path resolveDefaultBaseDir() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Path.of(localAppData, "DocSearch");
        }
        return Path.of(System.getProperty("user.home"), ".docsearch");
    }

    /**
     * <p>基準ディレクトリを返す（存在しなければ作成する）。</p>
     *
     * @return 基準ディレクトリ
     */
    public Path baseDir() {
        return ensure(baseDir);
    }

    /**
     * <p>検索対象フォルダ設定ファイルのパスを返す。</p>
     *
     * @return {@code folders.txt} の絶対パス
     */
    public Path settingsFile() {
        return baseDir().resolve("folders.txt");
    }

    /**
     * <p>Lucene 索引を格納するディレクトリを返す（存在しなければ作成する）。</p>
     *
     * @return 索引ディレクトリ
     */
    public Path indexDir() {
        return ensure(baseDir().resolve("index"));
    }

    /**
     * <p>最終索引時刻を記録するメタファイルのパスを返す。</p>
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
            throw new ServiceException("ディレクトリの作成に失敗しました: " + dir, e);
        }
        return dir;
    }
}
