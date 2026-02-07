package org.tcshare.app.amodule.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.tcshare.app.R;
import org.tcshare.widgets.stepview.FlowViewVertical;


public class WidgetStepViewVActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sv_v);
        FlowViewVertical vFlow = (FlowViewVertical) findViewById(R.id.vflow);
        vFlow.setProgress(9, 10, getResources().getStringArray(R.array.vflow), null);
    }
}
