package org.tcshare.app.amodule.activity;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.View;

import org.tcshare.app.R;
import org.tcshare.widgets.FavorLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class TCLiveLikeActivity extends AppCompatActivity {

    private FavorLayout favor;
    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                handler.sendEmptyMessageDelayed(1, 200);
                favor.addFavor();
            }
        }
    };

    @Override
    protected void onDestroy() {
        handler.removeMessages(1);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc_live_like);

        favor = findViewById(R.id.flavorLayout);
        Resources res = getResources();
        List<Drawable> items = new ArrayList<Drawable>(){
            {
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_1, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_2, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_3, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_4, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_5, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_6, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_7, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_8, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_9, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_10, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.gift_11, null));
            }
        };
        // 使用自定义的效果图标，默认使用♥形状
        favor.setFavors(items);
        // 设置效果图标，左右飘动的范围，以及终止点的范围
        favor.setFavorWidthHeight(100, 400);
        // 设置AnchorView，效果图标会从该AnchorView的中心点飘出
        favor.setAnchor(findViewById(R.id.ancher));


        // 自动添加效果图标
        handler.sendEmptyMessageDelayed(1, 200);

        // 手动点击添加
        findViewById(R.id.ancher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favor.addFavor();
            }
        });
    }
}
