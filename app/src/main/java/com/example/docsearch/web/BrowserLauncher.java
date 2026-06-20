package com.example.docsearch.web;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * <p>アプリ起動完了時に既定のブラウザで画面を開く。</p>
 *
 * <p>JRE同梱の配布物をダブルクリックで起動した利用者が、そのまま画面を使えるようにする。
 * {@code docsearch.browser.auto-open=false} で無効化できる（開発時など）。</p>
 */
@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    @Value("${server.port:8080}")
    private int port;

    @Value("${docsearch.browser.auto-open:true}")
    private boolean autoOpen;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = "http://localhost:" + port + "/";
        log.info("DocSearch を起動しました: {}", url);
        if (!autoOpen) {
            return;
        }
        openBrowser(url);
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception e) {
            log.debug("Desktop API でのブラウザ起動に失敗: {}", e.toString());
        }
        // Windows 向けフォールバック
        try {
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
        } catch (Exception e) {
            log.info("ブラウザを自動で開けませんでした。手動で {} を開いてください。", url);
        }
    }
}
