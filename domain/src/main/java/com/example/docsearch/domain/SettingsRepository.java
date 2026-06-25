package com.example.docsearch.domain;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * <p>検索対象フォルダの一覧を永続化するリポジトリ。</p>
 *
 * <p>1行1パスのテキストファイル（UTF-8）として保存する。空行は無視し、
 * 重複は保存時に取り除く（順序は保持）。</p>
 */
@Component
public class SettingsRepository {

    private final AppPaths appPaths;

    public SettingsRepository(AppPaths appPaths) {
        this.appPaths = appPaths;
    }

    /**
     * <p>保存済みの検索対象フォルダ一覧を返す。</p>
     *
     * @return フォルダパスの一覧（未設定なら空リスト）
     */
    public synchronized List<String> getFolders() {
        Path file = appPaths.settingsFile();
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            List<String> result = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String trimmed = line.strip();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("設定ファイルの読み込みに失敗しました", e);
        }
    }

    /**
     * <p>検索対象フォルダ一覧を保存する。空白除去・空要素除去・重複除去を行う。</p>
     *
     * @param folders 保存するフォルダパス
     * @return 正規化後に保存されたフォルダ一覧
     */
    public synchronized List<String> saveFolders(List<String> folders) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (folders != null) {
            for (String folder : folders) {
                if (folder == null) {
                    continue;
                }
                String trimmed = folder.strip();
                if (!trimmed.isEmpty()) {
                    normalized.add(trimmed);
                }
            }
        }
        List<String> result = new ArrayList<>(normalized);
        try {
            Files.write(appPaths.settingsFile(), result, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("設定ファイルの保存に失敗しました", e);
        }
        return result;
    }
}
