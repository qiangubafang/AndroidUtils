package org.tcshare.network;

import android.os.Build;
import android.os.FileUtils;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;

/**
 * 不确定用户提交数据的类型，及方式，只提供 sendRequest 方法.
 */
public class HttpApi {

    private static OkHttpClient client;
    private static final long CONECT_TIMEOUT = 10; // seconds
    private static final long READ_TIMEOUT = 30; // seconds
    private static final long WRITE_TIMEOUT = 300; // seconds
    public static final String UPLOAD_FILE_KEY = "file";
    public static final String UPLOAD_MULTI_FILE_KEY = "file[]";
    private static final HttpLogInterceptor debugInterceptor = new HttpLogInterceptor();
    
    public static void setDebug(boolean debug){
        debugInterceptor.debug = debug;
    }

    /**
     * 提供修改client的方法。
     */
    public static void setClient(OkHttpClient client) {
        HttpApi.client = client;
    }

    public static OkHttpClient getOkHttpClient() {
        if (client == null) {
            synchronized (HttpApi.class) {
                if (client == null) {

                    client = new OkHttpClient.Builder().connectTimeout(CONECT_TIMEOUT, TimeUnit.SECONDS)//设置超时时间
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写入超时时间
                            .addInterceptor(debugInterceptor)
                            .build();
                }
            }
        }
        return client;
    }


    public static OkHttpClient.Builder getProxyAuthBuilder(String proxyHost, int proxyPort, final String username, final String password) {
        Authenticator proxyAuthenticator = getProxyHeaderAuth(username, password);
        return new OkHttpClient.Builder().connectTimeout(CONECT_TIMEOUT, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写入超时时间
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)))
                .proxyAuthenticator(proxyAuthenticator)
                .addInterceptor(debugInterceptor);
    }


    public static Authenticator getProxyHeaderAuth(final String username, final String password) {
        return new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic(username, password);
                return response.request()
                        .newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            }
        };
    }

    public static Authenticator getHeaderAuth(final String username, final String password) {
        return new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic(username, password);
                return response.request()
                        .newBuilder()
                        .header("Authorization", credential)
                        .build();
            }
        };
    }

    /**
     * 获取headers
     * @return
     */
    public static Headers getHeaders(){
        return RequestBuilderFactory.getHeaders();
    }/**
     * 获取header
     * @param name
     * @return
     */
    public static String getHeader(String name){
        return RequestBuilderFactory.getHeaders().get(name);
    }

    /**
     * 清空，并设置
     * @param map
     */
    public static void setHeaders(Map<String,String> map){
        RequestBuilderFactory.setHeaders(Headers.of(map));
    }

    /**
     * 追加，如果存在，则更新
     * @param map
     */
    public static void addHeaders(Map<String,String> map){
        Headers headers = RequestBuilderFactory.getHeaders();
        Headers.Builder headersBuilder = headers.newBuilder();
        for(Map.Entry<String,String> entry : map.entrySet()) {
            headersBuilder.set(entry.getKey(), entry.getValue());
        }
        RequestBuilderFactory.setHeaders(headersBuilder.build());
    }

    /**
     * 追加，如果存在，则更新
     * @param name
     * @param value
     */
    public static void addHeader(String name, String value){
        Headers headers = RequestBuilderFactory.getHeaders();
        Headers.Builder headersBuilder = headers.newBuilder();
        headersBuilder.set(name, value);
        RequestBuilderFactory.setHeaders(headersBuilder.build());
    }

    /**
     * 清除headers
     */
    public static void clearHeaders() {
        RequestBuilderFactory.setHeaders(Headers.of());
    }
    /**
     * 移除header键值对
     * @param map
     */
    public static void removeHeaders(Map<String,String> map){
        Headers headers = RequestBuilderFactory.getHeaders();
        Headers.Builder headersBuilder = headers.newBuilder();
        for(Map.Entry<String,String> entry : map.entrySet()) {
            headersBuilder.removeAll(entry.getKey());
        }
        RequestBuilderFactory.setHeaders(Headers.of(map));
    }/**
     * 移除header键值对
     * @param name
     */
    public static void removeHeader(String name){
        Headers headers = RequestBuilderFactory.getHeaders();
        Headers.Builder headersBuilder = headers.newBuilder();
        headersBuilder.removeAll(name);
        RequestBuilderFactory.setHeaders(headersBuilder.build());
    }

    private static Map<String, String> beanToMap(Object bean) {
        if (bean == null) {
            return null;
        }

        Map<String, String> result = new HashMap<String, String>();
        Field[] fields = bean.getClass()
                .getFields();
        if (fields.length == 0) {
            return result;
        }

        for (Field field : fields) {
            //获取属性名称及值存入Map
            String key = field.getName();
            try {
                Object object = field.get(bean);
                if (object != null) {
                    String value = String.valueOf(field.get(bean));
                    result.put(key, value);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //获取父类属性
        fields = bean.getClass()
                .getSuperclass()
                .getFields();
        if (fields.length == 0) {
            return result;
        }

        for (Field field : fields) {
            //获取属性名称及值存入Map
            String key = field.getName();
            try {
                Object object = field.get(bean);
                if (object != null) {
                    String value = String.valueOf(field.get(bean));
                    result.put(key, value);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Object sendRequest(Request request, Callback callback) {
        getOkHttpClient().newCall(request)
                .enqueue(callback);
        return request.tag();
    }

    public static <T extends AResponse> Object sendRequest(Request request, T callback) {
        callback.onStart();
        getOkHttpClient().newCall(request)
                .enqueue(callback);
        return request.tag();
    }

    public static <T> T sendRequestSync(Request request, Class<T> tClass) {
        T result = null;
        Response response;
        try {
            response = getOkHttpClient().newCall(request).execute();
            String str = response.body().string(); // string 会关闭流，后面不用管
            if(tClass == String.class){
                result = tClass.getConstructor(String.class).newInstance(str);
            }else {
                result = new Gson().fromJson(str, tClass);
            }
        } catch (Exception e) {//json 格式化异常
            e.printStackTrace();
        }
        return result;
    }
    public static void download(Request request, File file){
        ResponseBody responseBody = null;
        FileOutputStream fos = null;
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            responseBody = response.body();
            // 数据流从网络到文件
            fos = new FileOutputStream(file);
            fos.write(responseBody.bytes());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(responseBody != null){
                try {
                    responseBody.close();
                }catch (Exception e){
                }
            }
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public static void downloadSyncByGet(String url, Map<String, String> params, File file) {
        Request.Builder getBuilder = RequestBuilderFactory.createGetRequestBuilder(url, params);
        download(getBuilder.build(), file);
    }
    public static void downloadSyncByGet(String url,  File file) {
        downloadSyncByGet(url, null, file);
    }

    //-----------sync start ----------------->>>>>>>>>>>>>>>>>>>>>>>
    public static <T> T getSync(String url, Class<T> tClass) {
        return getSync(url, null, tClass);
    }

    public static <T> T getSync(String url, Object params, Class<T> tClass) {
        return getSync(url, beanToMap(params), tClass);
    }

    public static <T> T getSync(String url, Map<String, String> params, Class<T> tClass) {
        Request.Builder getBuilder = RequestBuilderFactory.createGetRequestBuilder(url, params);
        return sendRequestSync(getBuilder.build(), tClass);
    }

    public static <T> T postSync(String url, Class<T> tClass) {
        return postSync(url, null, tClass);
    }

    public static <T> T postSync(String url, Object params, Class<T> tClass) {
        return postSync(url, beanToMap(params), tClass);
    }

    public static <T> T postSync(String url, Map<String, String> params, Class<T> tClass) {
        Request.Builder formBuilder = RequestBuilderFactory.createPostRequestBuilder(url, params);
        return sendRequestSync(formBuilder.build(), tClass);
    }

    public static <T> T postSync(String url, MediaType formType, Map<String, String> params, String fileKey, List<File> files, Class<T> tClass) {
        Request.Builder multiFormBuilder = RequestBuilderFactory.createMultiPostRequestBuilder(url, formType, fileKey, params, files);
        return sendRequestSync(multiFormBuilder.build(), tClass);
    }

    public static <T> T postSync(String url, Map<String, String> params, String fileKey, List<File> files, Class<T> tClass) {
        return postSync(url, MultipartBody.FORM, params, fileKey, files, tClass);
    }

    public static <T> T postSync(String url, Map<String, String> params, List<File> files, Class<T> tClass) {
        return postSync(url, params, UPLOAD_MULTI_FILE_KEY, files, tClass);
    }

    public static <T> T postSync(String url, Map<String, String> params, File[] files, Class<T> tClass) {
        return postSync(url, params, Arrays.asList(files), tClass);
    }

    public static <T> T postSyncJSON(String url, String json, Class<T> tClass) {
        Request.Builder jsonRequestBuilder = RequestBuilderFactory.createPostJsonRequestBuilder(url, json);
        return sendRequestSync(jsonRequestBuilder.build(), tClass);
    }

    public static <T> T postSyncJSON(String url, Object obj, Class<T> tClass) {
        return postSyncJSON(url, new Gson().toJson(obj), tClass);
    }

    //-----------async start ----------------->>>>>>>>>>>>>>>>>>>>>>>
    public static <T extends AResponse> void get(String url, T callBack) {
        get(url, null, callBack);
    }

    public static <T extends AResponse> void get(String url, Object params, T callBack) {
        get(url, beanToMap(params), callBack);
    }

    public static <T extends AResponse> void get(String url, Map<String, String> params, T callBack) {
        Request.Builder getBuilder = RequestBuilderFactory.createGetRequestBuilder(url, params);
        sendRequest(getBuilder.build(), callBack);
    }

    public static <T extends AResponse> void post(String url, T callBack) {
        post(url, null, callBack);
    }

    public static <T extends AResponse> void post(String url, Object params, T callBack) {
        post(url, beanToMap(params), callBack);
    }

    public static <T extends AResponse> void post(String url, Map<String, String> params, T callBack) {
        Request.Builder formBuilder = RequestBuilderFactory.createPostRequestBuilder(url, params);
        sendRequest(formBuilder.build(), callBack);
    }

    public static <T extends AResponse> void post(String url, MediaType formType, Map<String, String> params, String fileKey, List<File> files, T callBack) {
        Request.Builder multiFormBuilder = RequestBuilderFactory.createMultiPostRequestBuilder(url, formType, fileKey, params, files);
        sendRequest(multiFormBuilder.build(), callBack);
    }

    public static <T extends AResponse> void post(String url, Map<String, String> params, String fileKey, List<File> files, T callBack) {
        post(url, MultipartBody.FORM, params, fileKey, files, callBack);
    }

    public static <T extends AResponse> void post(String url, Map<String, String> params, List<File> files, T callBack) {
        post(url, params, UPLOAD_MULTI_FILE_KEY, files, callBack);
    }

    public static <T extends AResponse> void post(String url, Map<String, String> params, File[] files, T callBack) {
        post(url, params, Arrays.asList(files), callBack);
    }
    public static <T extends AResponse> void postJSON(String url, String json, T callBack) {
        Request.Builder jsonRequestBuilder = RequestBuilderFactory.createPostJsonRequestBuilder(url, json);
        sendRequest(jsonRequestBuilder.build(), callBack);

    }

    public static <T extends AResponse> void postJSON(String url, Object obj, T callBack) {
        postJSON(url, new Gson().toJson(obj), callBack);
    }

    public static <T extends AResponse> void del(String url, T callBack) {
        del(url, null, callBack);
    }

    public static <T extends AResponse> void del(String url, Map<String, String> params, T callBack) {
        Request.Builder delRequestBuilder = RequestBuilderFactory.createDelRequestBuilder(url, params);
        sendRequest(delRequestBuilder.build(), callBack);
    }



    public static <T extends AResponse> void put(String url, Map<String, String> params, T callBack) {
        Request.Builder formBuilder = RequestBuilderFactory.createPutRequestBuilder(url, params);
        sendRequest(formBuilder.build(), callBack);
    }


    public static <T extends AResponse> void upload(String url, String fileKey, List<File> files, T callBack) {
        post(url, null, fileKey, files, callBack);
    }

    public static <T extends AResponse> void upload(String url, List<File> files, T callBack) {
        post(url, null, UPLOAD_MULTI_FILE_KEY, files, callBack);
    }

    public static <T extends AResponse> void upload(String url, String fileKey, File file, T callBack) {
        List<File> files = new ArrayList<>();
        files.add(file);
        post(url, null, files, callBack);
    }

    public static <T extends AResponse> void upload(String url, File file, T callBack) {
        upload(url, UPLOAD_FILE_KEY, file, callBack);
    }

    public static <T extends AResponse> void upload(String url, String fileKey, File[] file, T callBack) {
        upload(url, fileKey, file, callBack);
    }

    public static <T extends AResponse> void upload(String url, File[] file, T callBack) {
        upload(url, UPLOAD_MULTI_FILE_KEY, file, callBack);
    }

}
