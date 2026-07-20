package com.example.docsearch.domain.service.lucene;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;

/**
 * <p>Lucene 索引の読み込みで共通する「未索引なら既定値を返し、索引済みなら
 * {@link DirectoryReader} を開いて処理する」というパターンをまとめる。</p>
 */
public final class LuceneReaders {

    private LuceneReaders() {
        // インスタンス化を防ぐためのコンストラクタ
    }

    /**
     * <p>索引が存在すれば {@link DirectoryReader} を開いて {@code action} を適用し、
     * 存在しなければ {@code fallback} を返す。readerは処理後に自動的にクローズされる。</p>
     *
     * @param dir 索引ディレクトリ
     * @param fallback 未索引時に返す値
     * @param action readerを使った処理
     * @return actionの結果、または未索引時は{@code fallback}
     * @throws IOException 索引の読み込みに失敗した場合
     */
    public static <T> T withReader(Directory dir, T fallback, IndexReaderFunction<T> action) throws IOException {
        if (!DirectoryReader.indexExists(dir)) {
            return fallback;
        }
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            return action.apply(reader);
        }
    }

    @FunctionalInterface
    public interface IndexReaderFunction<T> {
        T apply(DirectoryReader reader) throws IOException;
    }
}
