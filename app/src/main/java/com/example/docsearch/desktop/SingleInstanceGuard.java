package com.example.docsearch.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>多重起動を防ぐためのガード。</p>
 *
 * <p>起動時にループバック専用のロックポートを確保する。確保できれば最初のインスタンスであり、
 * 確保できなければ既に別のインスタンスが起動しているとみなす。後者の場合は既存インスタンスへ
 * 「画面を開け」と通知してから終了することで、2つ目の JVM が立ち上がるのを防ぐ。</p>
 *
 * <p>これにより、ブラウザのタブを閉じても常駐しているアプリを利用者が二重に起動して
 * ポートを奪い合う事故を防止する。</p>
 */
public final class SingleInstanceGuard implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleInstanceGuard.class);

    /** 単一インスタンス判定に用いるループバック専用ポート（アプリ本体の 8080 とは別）。 */
    private static final int LOCK_PORT = 49213;

    /** 既存インスタンスに画面表示を依頼するコマンド。 */
    private static final String CMD_SHOW = "SHOW";

    private final ServerSocket lockSocket;
    private final AtomicReference<Runnable> showAction = new AtomicReference<>();
    private final AtomicBoolean pendingShow = new AtomicBoolean(false);

    private SingleInstanceGuard(ServerSocket lockSocket) {
        this.lockSocket = lockSocket;
    }

    /**
     * <p>ロックの取得を試みる。</p>
     *
     * @return 取得できた場合はガード本体。既に別インスタンスが起動済みの場合は {@code null}
     *         （呼び出し側はそのまま終了すること）。
     */
    public static SingleInstanceGuard tryAcquire() {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        ServerSocket socket;
        try {
            socket = new ServerSocket(LOCK_PORT, 1, loopback);
        } catch (IOException alreadyRunning) {
            // 既に起動済み。既存インスタンスへ通知して、このプロセスは起動を中止する。
            notifyExistingInstance(loopback);
            return null;
        }
        SingleInstanceGuard guard = new SingleInstanceGuard(socket);
        guard.startListener();
        // JVM 終了時に確実にロックを解放する。
        Runtime.getRuntime().addShutdownHook(new Thread(guard::close, "single-instance-cleanup"));
        return guard;
    }

    private static void notifyExistingInstance(InetAddress loopback) {
        try (Socket client = new Socket(loopback, LOCK_PORT);
             OutputStream out = client.getOutputStream()) {
            out.write((CMD_SHOW + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            LOGGER.info("DocSearch は既に起動しています。既存インスタンスの画面を開きます。");
        } catch (IOException e) {
            LOGGER.warn("既存インスタンスへの通知に失敗しました: {}", e.toString());
        }
    }

    private void startListener() {
        Thread thread = new Thread(this::listenLoop, "single-instance-listener");
        thread.setDaemon(true);
        thread.start();
    }

    private void listenLoop() {
        while (!lockSocket.isClosed()) {
            try (Socket client = lockSocket.accept();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {
                String command = reader.readLine();
                if (CMD_SHOW.equals(command)) {
                    Runnable action = showAction.get();
                    if (action != null) {
                        action.run();
                    } else {
                        pendingShow.set(true);
                    }
                }
            } catch (IOException e) {
                if (!lockSocket.isClosed()) {
                    LOGGER.debug("単一インスタンス通知の受信に失敗: {}", e.toString());
                }
            }
        }
    }

    /**
     * <p>別インスタンスから画面表示を要求されたときに実行する処理を設定する。</p>
     *
     * <p>アプリの起動が完了し、実際の URL が定まってから設定する。</p>
     *
     * @param action 実行する処理
     */
    public void setShowAction(Runnable action) {
        showAction.set(action);
        if (pendingShow.getAndSet(false)) {
            action.run();
        }
    }

    @Override
    public void close() {
        try {
            if (!lockSocket.isClosed()) {
                lockSocket.close();
            }
        } catch (IOException e) {
            LOGGER.debug("ロックソケットのクローズに失敗: {}", e.toString());
        }
    }
}
