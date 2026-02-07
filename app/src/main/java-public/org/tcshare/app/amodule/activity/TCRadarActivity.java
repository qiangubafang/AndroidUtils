package org.tcshare.app.amodule.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.tcshare.app.R;
import org.tcshare.utils.RandomUtils;
import org.tcshare.widgets.RadarView;

public class TCRadarActivity extends AppCompatActivity {
    private RadarView radarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcradar_main);

        radarView = findViewById(R.id.radarView);
        radarView.setSearching(true);
        addPoint();


    }
    private void addPoint(){
        radarView.postDelayed(new Runnable() {
            @Override
            public void run() {
                radarView.addPoint(RandomUtils.getRandomFloat(1));
                addPoint();
            }
        }, 50);

    }

}