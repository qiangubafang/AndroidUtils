package org.tcshare.network;

import android.content.Context;


import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by FallRain on 2017/11/2.
 */

public abstract class ResponseString extends AResponse<String> {

    public ResponseString(){
        super();
    }

    public ResponseString(Context ctx){
        super(ctx);
    }

    public ResponseString(Context ctx, boolean immerse) {
        super(ctx, immerse);
    }

    @Override
    protected String processResponce(Call call, Response response) {
        String result = null;
        try {
            result = response.body().string();  // string 会关闭流，后面不用管
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

}