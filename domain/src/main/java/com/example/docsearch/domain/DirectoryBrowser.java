package com.example.docsearch.domain;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.example.docsearch.domain.model.DirectoryEntry;

/**
 * <p>サーバ（＝同一PC）のファイルシステムを画面から辿るためのディレクトリ走査。</p>
 *
 * <p>ブラウザでは実フォルダパスを取得できないため、サーバ側でドライブ／サブフォルダ
 * 一覧を提供し、画面から階層をたどってフォルダを選択させる。</p>
 */
@Component
public class DirectoryBrowser {

    /**
     * ルート（Windows のドライブ等）の一覧を返す。
     *
     * @return ルートエントリの一覧
     */
    public List<DirectoryEntry> listRoots() {
        List<DirectoryEntry> roots = new ArrayList<>();
        for (File root : File.listRoots()) {
            String path = root.getAbsolutePath();
            roots.add(new DirectoryEntry(path, path));
        }
        return roots;
    }

    /**
     * 指定パス直下のサブディレクトリ一覧を返す（隠し/アクセス不可は除外）。
     *
     * @param pathStr 対象ディレクトリの絶対パス
     * @return サブディレクトリの一覧（名前昇順）
     * @throws IllegalArgumentException ディレクトリでない場合
     */
    public List<DirectoryEntry> listChildren(String pathStr) {
        Path dir = Path.of(pathStr);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("ディレクトリではありません: " + pathStr);
        }
        List<DirectoryEntry> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                try {
                    if (Files.isDirectory(child) && !Files.isHidden(child)) {
                        Path name = child.getFileName();
                        entries.add(new DirectoryEntry(
                                name == null ? child.toString() : name.toString(),
                                child.toAbsolutePath().toString()));
                    }
                } catch (IOException ignore) {
                    // アクセス不可などの個別エントリはスキップ
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("フォルダ一覧の取得に失敗しました: " + pathStr, e);
        }
        entries.sort(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)));
        return entries;
    }

    /**
     * 親ディレクトリのパスを返す。ルート直下（親なし）なら {@code null}。
     *
     * @param pathStr 対象パス
     * @return 親ディレクトリの絶対パス、なければ null
     */
    public String parentOf(String pathStr) {
        Path parent = Path.of(pathStr).getParent();
        return parent == null ? null : parent.toString();
    }
}
