package com.example.docsearch.domain.model;

/**
 * <p>検索でヒットした1ファイルを表す不変モデル。</p>
 *
 * <p>DocSearch のビジネス概念であるため domain に置く。</p>
 *
 * @param fileName             ファイル名（拡張子含む）
 * @param absolutePath         ファイルの絶対パス
 * @param parentPath           親ディレクトリの絶対パス
 * @param extension            拡張子（小文字、ドットなし。無い場合は空文字）
 * @param sizeBytes            ファイルサイズ（バイト）
 * @param lastModifiedEpochMs  最終更新時刻（エポックミリ秒）
 * @param score                Lucene の関連度スコア（高いほど一致度が高い）
 * @param snippet              本文の抜粋（一致箇所を {@code <mark>} で強調。HTMLエスケープ済み）
 */
public record SearchHit(
        String fileName,
        String absolutePath,
        String parentPath,
        String extension,
        long sizeBytes,
        long lastModifiedEpochMs,
        double score,
        String snippet
) {
}
