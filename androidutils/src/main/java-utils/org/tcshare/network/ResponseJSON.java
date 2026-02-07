package org.tcshare.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by FallRain on 2017/11/2.
 * ResponseJSONArray： 这里的根节点是个对象 {}
 */

public abstract class ResponseJSON<T> extends AResponse<T> {
    private static final String TAG = ResponseJSON.class.getSimpleName();

    public ResponseJSON() {
        super();
    }

    public ResponseJSON(Context ctx) {
        super(ctx);
    }

    public ResponseJSON(Context ctx, boolean immerse) {
        super(ctx, immerse);
    }

    protected T processResponce(Call call, Response response) {
        T result = null;
        try {
            String str = response.body().string();  // string 会关闭流，后面不用管
            result = new Gson().fromJson(str, (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}