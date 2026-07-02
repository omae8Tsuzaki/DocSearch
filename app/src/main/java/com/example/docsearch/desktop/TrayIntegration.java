package com.example.docsearch.desktop;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * <p>アプリをシステムトレイに常駐させ、起動完了時にブラウザで画面を開く。</p>
 *
 * <p>Web アプリのままだとブラウザのタブを閉じても常駐に気づけず、終了手段も分かりにくい。
 * トレイアイコンを常時のインジケータ兼操作 UI とし、「開く」「終了」を提供することで、
 * 非開発者でも稼働状況の把握と正常終了ができるようにする。</p>
 *
 * <p>{@code docsearch.browser.auto-open=false} で起動時の自動オープンを無効化できる（開発時など）。</p>
 */
@Component
public class TrayIntegration implements ApplicationListener<ApplicationReadyEvent> {

    // ログ出力の設定
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayIntegration.class);

    @Value("${server.port:8421}")
    private int port;

    @Value("${docsearch.browser.auto-open:true}")
    private boolean autoOpen;

    private final ApplicationContext applicationContext;
    private final ObjectProvider<SingleInstanceGuard> guardProvider;

    private TrayIcon trayIcon;

    public TrayIntegration(ApplicationContext applicationContext,
                           ObjectProvider<SingleInstanceGuard> guardProvider) {
        this.applicationContext = applicationContext;
        this.guardProvider = guardProvider;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = "http://localhost:" + port + "/";
        LOGGER.info("DocSearch を起動しました: {}", url);

        // 二度目以降の起動から「画面を開け」と通知されたら、このインスタンスでブラウザを開く。
        SingleInstanceGuard guard = guardProvider.getIfAvailable();
        if (guard != null) {
            guard.setShowAction(() -> BrowserOpener.open(url));
        }

        installTrayIcon(url);

        if (autoOpen) {
            BrowserOpener.open(url);
        }
    }

    private void installTrayIcon(String url) {
        if (!SystemTray.isSupported()) {
            LOGGER.info("システムトレイ非対応の環境のため、トレイ常駐は行いません。終了はウィンドウやタスクから行ってください。");
            return;
        }
        try {
            PopupMenu popup = new PopupMenu();

            MenuItem openItem = new MenuItem("DocSearch を開く");
            openItem.addActionListener(e -> BrowserOpener.open(url));
            popup.add(openItem);

            popup.addSeparator();

            MenuItem exitItem = new MenuItem("終了");
            exitItem.addActionListener(e -> shutdown());
            popup.add(exitItem);

            trayIcon = new TrayIcon(createTrayImage(), "DocSearch", popup);
            trayIcon.setImageAutoSize(true);
            // アイコンのダブルクリックでも画面を開く。
            trayIcon.addActionListener(e -> BrowserOpener.open(url));

            SystemTray.getSystemTray().add(trayIcon);
            LOGGER.info("システムトレイに常駐しました。終了はトレイアイコンの「終了」から行ってください。");
        } catch (AWTException e) {
            LOGGER.warn("システムトレイへの登録に失敗しました: {}", e.toString());
        }
    }

    private void shutdown() {
        LOGGER.info("DocSearch を終了します。");
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        // Spring コンテキストを正常に閉じてから（索引処理などを安全に停止）JVM を終了する。
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    /**
     * <p>トレイ用の簡易アイコン画像をプログラムで生成する（アイコンリソース未整備のため）。</p>
     *
     * @return 16x16 のトレイアイコン画像
     */
    private Image createTrayImage() {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x15, 0x65, 0xC0));
        g.fillRoundRect(0, 0, size - 1, size - 1, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("D", 4, 12);
        g.dispose();
        return image;
    }
}
