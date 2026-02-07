package org.tcshare.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by FallRain on 2017/11/2.
 *
 * 与ResponseJSON区别： 这里的根节点是个数组 [] (list 或map）, 返回一个ArrayList<T>的列表 or null
 */


public abstract class ResponseJSONList<T> extends AResponse<T> {
    private static final String TAG = ResponseJSONList.class.getSimpleName();

    public ResponseJSONList() {
        super();
    }

    public ResponseJSONList(Context ctx) {
        super(ctx);
    }

    public ResponseJSONList(Context ctx, boolean immerse) {
        super(ctx, immerse);
    }


    protected T processResponce(Call call, Response response) {
        return null;
    }

    protected List<T> processResponceList(Call call, okhttp3.Response response){
        List<T> result = null;
        try {
            String str = response.body().string();  // string 会关闭流，后面不用管
            Class<T> tClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            result = new Gson().fromJson(str, TypeToken.getParameterized(ArrayList.class, tClass).getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onResponseUI(Call call, T processObj){
    }

    public abstract void onResponseUI(Call call, List<T> processObj);


    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        nextUI(call, e);
    }

    @Override
    public void onResponse(Call call, okhttp3.Response response) throws IOException {
        List<T> obj = processResponceList(call, response);
        nextUI(call, obj);
    }



    private void nextUI(final Call call, final List<T> body) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onResponseUI(call, body);
                onFinished();
            }
        });
    }



}
