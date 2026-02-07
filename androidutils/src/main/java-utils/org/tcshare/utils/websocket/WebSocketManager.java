package org.tcshare.utils.websocket;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class WebSocketManager extends Observable {
    private final static String TAG = WebSocketManager.class.getSimpleName();
    private final static int RE_CONNECT = 5000;     // 重连间隔时间，毫秒
    private static volatile WebSocketManager mInstance = null;

    private OkHttpClient client;
    private Request request;
    private IReceiveMessage receiveMessage;
    private WebSocket mWebSocket;

    private boolean isConnect = false;
    //心跳包发送时间计时
    private long sendTime = 0L;
    // 发送心跳包
    private final Handler mHandler = new Handler();
    // 每隔40秒发送一次心跳包，检测连接没有断开
    private long heartRate = 30_000;


    private WebSocketManager() {
    }

    public static WebSocketManager getInstance() {
        if (null == mInstance) {
            synchronized (WebSocketManager.class) {
                if (mInstance == null) {
                    mInstance = new WebSocketManager();
                }
            }
        }
        return mInstance;
    }


    public void init(String wsUrl, long heartRate, IReceiveMessage message) {
        this.heartRate = heartRate < 1000 ? 1000 : heartRate; // 心跳间隔至少1秒以上

        client = new OkHttpClient.Builder()
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
        Log.d(TAG, "WebSocket prepare:" + wsUrl);
        request = new Request.Builder().url(wsUrl).build();
        receiveMessage = message;
        connect();
    }

    /**
     * 连接
     */
    public void connect() {
        if (isConnected()) {
            Log.d(TAG, "WebSocket 已经连接！");
            Logger.d("WebSocket 已经连接！");
            return;
        }
        Log.d(TAG, "WebSocket connect...");
        client.newWebSocket(request, createListener());
    }

    /**
     * 重连
     */
    public void reconnect() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "WebSocket reconnect...");
                connect();
            }
        }, RE_CONNECT);


    }

    /**
     * 是否连接
     */
    public boolean isConnected() {
        return mWebSocket != null && isConnect;
    }


    // 发送心跳包
    private final Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= heartRate) {
                sendTime = System.currentTimeMillis();
                boolean isSend = sendMessage(getWSHeart());
                Log.d(TAG, "心跳是否发送成功" + isSend);
                Logger.d("心跳是否发送成功" + isSend);
            }
            mHandler.postDelayed(this, heartRate); //每隔一定的时间，对长连接进行一次心跳检测
        }
    };

    private String getWSHeart() {
        return "{\"heart\":" + System.currentTimeMillis() + "}";
    }

    /**
     * 发送消息
     *
     * @param text 字符串
     * @return boolean
     */
    public boolean sendMessage(String text) {
        if (!isConnected()) return false;
        return mWebSocket.send(text);
    }

    /**
     * 发送消息
     *
     * @param byteString 字符集
     * @return boolean
     */
    public boolean sendMessage(ByteString byteString) {
        if (!isConnected()) return false;
        return mWebSocket.send(byteString);
    }

    /**
     * 关闭连接
     */
    public void close() {
        Log.d(TAG, "WebSocket close, 主动关闭");
        Logger.d("WebSocket close, 主动关闭");
        if (isConnected()) {
            mWebSocket.cancel();
            mWebSocket.close(1001, "客户端主动关闭连接");
        }

        mHandler.removeCallbacksAndMessages(null);
    }

    private WebSocketListener createListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                Log.d(TAG, "WebSocket 打开:" + response);
                Logger.d("WebSocket 打开:" + response);
                mWebSocket = webSocket;
                isConnect = response.code() == 101;
                if (!isConnect) {
                    reconnect();
                } else {
                    Log.d(TAG, "WebSocket 连接成功!");
                    Logger.d("WebSocket 连接成功!");
                    if (receiveMessage != null) {
                        receiveMessage.onConnectSuccess();
                    }
                    if (sendMessage(getWSHeart())) {
                        mHandler.postDelayed(heartBeatRunnable, heartRate);
                    }
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                if (receiveMessage != null) {
                    receiveMessage.onMessage(text);
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                if (receiveMessage != null) {
                    receiveMessage.onMessage(bytes.base64());
                }
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
                Log.d(TAG, "WebSocket onClosing!");
                Logger.d("WebSocket onClosing!");
                mWebSocket = null;
                isConnect = false;
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (receiveMessage != null) {
                    receiveMessage.onClose();
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d(TAG, "WebSocket onClosed!");
                Logger.d("WebSocket onClosed!");
                mWebSocket = null;
                isConnect = false;
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (receiveMessage != null) {
                    receiveMessage.onClose();
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                if (response != null) {
                    Log.d(TAG, "WebSocket 连接失败：" + response.message());
                    Logger.d("WebSocket 连接失败：" + response.message());
                }
                Log.d(TAG, "WebSocket 连接失败异常原因：" + t.getMessage());
                Logger.d("WebSocket 连接失败异常原因：" + t.getMessage());
                isConnect = false;
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
                if (receiveMessage != null) {
                    receiveMessage.onConnectFailed();
                }
                if (t.getMessage() != null && !t.getMessage().equals("Socket closed")) {
                    reconnect();
                }

            }
        };
    }
}

