package com.example.docsearch.domain;

import java.nio.file.Path;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <p>各種ファイルから本文テキストを抽出する。</p>
 *
 * <p>Apache Tika を用い、pptx/xlsx/docx/pdf/md/txt などを統一的に扱う。
 * 抽出に失敗したファイルは空文字を返し、索引化自体は継続させる
 * （ファイル名検索は引き続き可能にするため）。</p>
 */
@Component
public class TextExtractor {

    // ログ出力の設定
    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractor.class);

    /** 抽出する本文の最大文字数（索引肥大化と処理時間の抑制）。 */
    private static final int MAX_CHARS = 200_000;
    /** 本文抽出を行う最大ファイルサイズ（これを超えるファイルは本文をスキップ）。 */
    private static final long MAX_FILE_BYTES = 50L * 1024 * 1024;

    private final Tika tika;

    public TextExtractor() {
        this.tika = new Tika();
        this.tika.setMaxStringLength(MAX_CHARS);
    }

    /**
     * <p>本文テキストを抽出する。</p>
     *
     * @param file      対象ファイル
     * @param sizeBytes ファイルサイズ
     * @return 抽出した本文（失敗・スキップ時は空文字）
     */
    public String extract(Path file, long sizeBytes) {
        if (sizeBytes > MAX_FILE_BYTES) {
            return "";
        }
        try {
            String text = tika.parseToString(file);
            return text == null ? "" : text;
        } catch (Exception e) {
            // 破損ファイルや未対応形式などはスキップ（本文なしで索引化を続行）
            LOGGER.debug("本文抽出に失敗（スキップ）: {} ({})", file, e.toString());
            return "";
        }
    }
}
