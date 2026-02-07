package org.tcshare.app.amodule.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import org.tcshare.app.R;
import org.tcshare.widgets.GaugeView;

public class TCGaugeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc_gauge);

        GaugeView gaugeView = findViewById(R.id.gaugu);

        GaugeView.Config cfg = new GaugeView.Config();
        cfg.labelText = "温度"; // 所有项均可在此配置修改
        gaugeView.config(cfg);

        // 中间值格式，最大值，最小值，刻度线（dialStep的值，必须被最大值减最小值整除），第一个警戒线，第二个警戒线
        gaugeView.init("%.1f°C", 100f, -30f, 10, 10, 70);
        gaugeView.updateVal(30f);
    }
}