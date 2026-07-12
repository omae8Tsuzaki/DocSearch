package com.example.docsearch.domain;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * <p>設定値を管理するクラス。</p>
 */
@Component
public class DomainConfig {

    /**
     * <p>検索結果の最大件数。</p>
     */
    @Value("${docSearch.search.maxLimit}")
    private int searchMaxLimit;

    /**
     * <p>検索対象のファイルの拡張子。</p>
     */
    @Value("${docSearch.search.supportedExtensions}")
    private Set<String> supportedExtensions;

    @PostConstruct
    private void init() {
        supportedExtensions = Set.copyOf(supportedExtensions);
    }

    /**
     * <p>検索結果の最大件数を取得する。</p>
     *
     * @return 検索結果の最大件数
     */
    public int getSearchMaxLimit() {
        return searchMaxLimit;
    }

    /**
     * <p>検索対象のファイルの拡張子を取得する。</p>
     *
     * @return 検索対象のファイルの拡張子
     */
    public Set<String> getSupportedExtensions() {
        // SpotBugs の EI（Expose Internal Representation）ルールに関する警告対応。
        return supportedExtensions;
    }
}
