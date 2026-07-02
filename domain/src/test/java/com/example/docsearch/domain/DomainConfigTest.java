package com.example.docsearch.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>{@link DomainConfig} のテストクラス。</p>
 */
@SpringBootTest(properties = "spring.config.import=classpath:domain-config.yml")
@ContextConfiguration(classes = {DomainConfig.class})
public class DomainConfigTest {

    @Autowired
    private DomainConfig config;

    /**
     * <p>正常系。</p>
     *
     * @throws Exception 想定外の例外が発生した場合
     */
    @Test
    public void getterSuccess01() throws Exception {

        //
        // 実行・検証
        //
        assertEquals(200, config.getSearchMaxLimit());
        assertEquals(Set.of("txt", "md", "log", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "xml"),
                config.getSupportedExtensions());
    }
}
