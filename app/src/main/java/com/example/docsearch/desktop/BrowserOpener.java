package com.example.docsearch.desktop;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>既定のブラウザで URL を開くためのユーティリティ。</p>
 *
 * <p>トレイメニュー・起動時の自動オープン・多重起動時の通知から共通で利用する。</p>
 */
public final class BrowserOpener {

    // ログ出力の設定
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserOpener.class);

    private BrowserOpener() {
    }

    /**
     * <p>指定 URL を既定ブラウザで開く。失敗しても例外は送出しない。</p>
     *
     * @param url 開く URL
     */
    public static void open(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception e) {
            LOGGER.debug("Desktop API でのブラウザ起動に失敗: {}", e.toString());
        }
        // Windows 向けフォールバック
        try {
            new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
        } catch (Exception e) {
            LOGGER.info("ブラウザを自動で開けませんでした。手動で {} を開いてください。", url);
        }
    }
}
