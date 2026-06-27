package com.example.docsearch.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>設定値を管理するクラス。</p>
 */
@Component
public class DomainConfig {

    /**
     * <p>検索結果の最大件数を取得する。</p>
     */
    @Value("${docSearch.search.maxLimit}")
    private int searchMaxLimit;

    /**
     * <p>検索結果の最大件数を取得する。</p>
     *
     * @return 検索結果の最大件数
     */
    public int getSearchMaxLimit() {
        return searchMaxLimit;
    }
}
