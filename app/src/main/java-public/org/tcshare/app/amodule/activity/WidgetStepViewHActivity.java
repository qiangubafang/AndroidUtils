package org.tcshare.app.amodule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;

import org.tcshare.app.R;
import org.tcshare.widgets.stepview.FlowViewHorizontal;

import java.util.HashMap;
import java.util.Map;

public class WidgetStepViewHActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sv_h);
        FlowViewHorizontal hFlow3 = (FlowViewHorizontal) findViewById(R.id.hflowview3);
        FlowViewHorizontal hFlow4 = (FlowViewHorizontal) findViewById(R.id.hflowview4);
        FlowViewHorizontal hFlow5 = (FlowViewHorizontal) findViewById(R.id.hflowview5);
        FlowViewHorizontal hFlow6 = (FlowViewHorizontal) findViewById(R.id.hflowview6);

        Map<String, String> map = new HashMap<>();
        map.put("异常", "#FF0000");

        hFlow3.setProgress(3, 4, null, null);

        hFlow4.setProgress(5, 5, getResources().getStringArray(R.array.hflow), null);
        hFlow4.setKeyColorByName(map, false);

        hFlow5.setProgress(4, 5, getResources().getStringArray(R.array.htime5), null);

        hFlow6.setProgress(5, 5, getResources().getStringArray(R.array.hflow6), getResources().getStringArray(R.array.htime6));
        Map<String, String> map1 = new HashMap<>();
        map1.put("接单", "#009999");
        map1.put("取件", "#A65100");
        map1.put("配送", "#620CAC");
        map1.put("完成", "#00733E");
        hFlow6.setKeyColorByName(map1, false);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WidgetStepViewHActivity.this, WidgetStepViewVActivity.class));
            }
        });
    }
}
