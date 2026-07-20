package com.example.docsearch.domain.service.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>索引・検索の両方で使う日本語アナライザを、アプリ全体で1つ共有するための設定。</p>
 */
@Configuration
public class LuceneAnalyzerConfig {

    /**
     * <p>{@link JapaneseAnalyzer} をSingleton Beanとして提供する。
     * コンテナ終了時には {@code close()} が自動的に呼ばれる。</p>
     *
     * @return 日本語アナライザ
     */
    @Bean
    public Analyzer japaneseAnalyzer() {
        return new JapaneseAnalyzer();
    }
}
