package com.example.docsearch;

import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.docsearch.desktop.SingleInstanceGuard;
import com.example.docsearch.domain.AppPaths;

/**
 * <p>アプリケーションを起動するメインクラス。</p>
 */
@SpringBootApplication
public class DocSearchApplication {

    public static void main(String[] args) {
        // exe 配布版はコンソールを表示しないため、データ保存先と同じ場所へログをファイル出力する。
        // Spring のログ初期化より前にシステムプロパティを設定する必要がある。
        Path logFile = AppPaths.defaultBaseDir().resolve("logs").resolve("docsearch.log");
        System.setProperty("docsearch.log.file", logFile.toString());

        // 多重起動ガード：既に起動済みなら既存インスタンスに画面表示を通知し、このプロセスは終了する。
        SingleInstanceGuard guard = SingleInstanceGuard.tryAcquire();
        if (guard == null) {
            return;
        }

        SpringApplication application = new SpringApplication(DocSearchApplication.class);
        // システムトレイ常駐・ブラウザ起動に AWT を用いるため、ヘッドレスを無効化する。
        application.setHeadless(false);
        // 取得済みのロックを Bean として登録し、トレイ統合（TrayIntegration）から参照できるようにする。
        application.addInitializers((ApplicationContextInitializer<ConfigurableApplicationContext>) ctx ->
                ctx.getBeanFactory().registerSingleton("singleInstanceGuard", guard));
        application.run(args);
    }
}
