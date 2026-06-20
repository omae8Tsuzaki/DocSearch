package com.example.docsearch.domain.model;

/**
 * <p>索引の現在状態。</p>
 *
 * @param indexing            索引作成が実行中かどうか
 * @param docCount            索引済みドキュメント数
 * @param lastIndexedEpochMs  最終索引完了時刻（エポックミリ秒。未実施なら 0）
 */
public record IndexStatus(
        boolean indexing,
        int docCount,
        long lastIndexedEpochMs
) {
}
