package org.tcshare.network;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by kaiyim on 2017/7/9 0009.
 */

public class HttpLogInterceptor implements okhttp3.Interceptor {
    private static final String TAG = HttpLogInterceptor.class.getSimpleName();
    public boolean debug;

    public HttpLogInterceptor() {
        this(false);
    }

    public HttpLogInterceptor(boolean debug) {
        this.debug = debug;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        Response res = chain.proceed(req);

        if (debug) {
            try {
                Log.d(TAG, String.format("...\n请求链接：%s\n请求头：%s\n请求参数：%s\n响应头：%s\n请求响应：%s",
                        req.url(), new Gson().toJson(req.headers()), getRequestInfo(req), new Gson().toJson(res.headers()), getResponseInfo(res)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res;
    }


    /**
     * 打印请求消息
     *
     * @param request 请求的对象
     */
    private String getRequestInfo(Request request) {
        String str = "";
        if (request == null) {
            return str;
        }
        RequestBody requestBody = request.body();
        if (requestBody == null) {
            return str;
        }
        try {
            Buffer bufferedSink = new Buffer();
            requestBody.writeTo(bufferedSink);
            Charset charset = Charset.forName("utf-8");
            str = bufferedSink.readString(charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 打印返回消息
     *
     * @param response 返回的对象
     */
    private String getResponseInfo(Response response) {
        String str = "";
        if (response == null || !response.isSuccessful()) {
            return str;
        }
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();
        try {
            source.request(Long.MAX_VALUE); // Buffer the entire body.
        } catch (IOException e) {
            e.printStackTrace();
        }
        Buffer buffer = source.buffer();
        Charset charset = Charset.forName("utf-8");
        if (contentLength != 0) {
            str = buffer.clone().readString(charset);
        }
        return str;
    }

}
