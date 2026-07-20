package com.example.docsearch.domain.service.lucene;

import com.example.docsearch.domain.service.FullTextSearchService;
import com.example.docsearch.domain.service.LuceneIndexService;

/**
 * <p>Lucene ドキュメントのフィールド名を定義する定数クラス。</p>
 *
 * <p>索引作成（{@link LuceneIndexService}）と検索（{@link FullTextSearchService}）で
 * 共有する。</p>
 */
public final class LuceneFields {

    /** ファイルの絶対パス（一意キー）。 */
    public static final String PATH = "path";
    /** ファイル名（トークン化して検索対象）。 */
    public static final String NAME = "filename";
    /** 本文（トークン化して検索対象＋抜粋表示用に保存）。 */
    public static final String CONTENT = "content";
    /** 拡張子（小文字）。 */
    public static final String EXTENSION = "extension";
    /** 親ディレクトリの絶対パス。 */
    public static final String PARENT = "parent";
    /** ファイルサイズ（バイト）。 */
    public static final String SIZE = "size";
    /** 最終更新時刻（エポックミリ秒）。差分索引の判定に使う。 */
    public static final String MODIFIED = "modified";

    private LuceneFields() {
    }
}
