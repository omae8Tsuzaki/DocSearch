package com.example.docsearch.domain.model;

/**
 * <p>1回の索引作成（再索引）の結果。</p>
 *
 * @param indexed   新規／更新で索引化した件数
 * @param skipped   更新なしでスキップした件数
 * @param removed   削除済みファイルとして索引から除去した件数
 * @param failed    索引化に失敗した件数
 * @param elapsedMs 所要時間（ミリ秒）
 */
public record IndexResult(
        int indexed,
        int skipped,
        int removed,
        int failed,
        long elapsedMs
) {
}
