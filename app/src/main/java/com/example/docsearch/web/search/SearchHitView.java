package com.example.docsearch.web.search;

import com.example.docsearch.domain.model.SearchHit;

/**
 * <p>{@link SearchHit} をテンプレート表示用に整形したビューモデル。</p>
 *
 * <p>サイズ・日時のフォーマットはテンプレート側では行わず、ここで完結させる。</p>
 *
 * @param fileName   ファイル名（拡張子含む）
 * @param parentPath 親ディレクトリの絶対パス
 * @param sizeText   整形済みのファイルサイズ（例: {@code "12.3 KB"}）
 * @param dateText   整形済みの最終更新日時（例: {@code "2026-08-11 09:30"}）
 * @param snippet    本文の抜粋（{@code <mark>} のみを含む、HTMLエスケープ済みの文字列）
 */
public record SearchHitView(
        String fileName,
        String parentPath,
        String sizeText,
        String dateText,
        String snippet
) {

    /**
     * <p>{@link SearchHit} からビューモデルを組み立てる。</p>
     *
     * @param hit 検索ヒット
     * @return ビューモデル
     */
    public static SearchHitView from(SearchHit hit) {
        return new SearchHitView(
                hit.fileName(),
                hit.parentPath(),
                ViewFormatters.formatSize(hit.sizeBytes()),
                ViewFormatters.formatDateTime(hit.lastModifiedEpochMs()),
                hit.snippet()
        );
    }
}
