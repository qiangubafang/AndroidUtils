package org.tcshare.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by FallRain on 2017/8/18.
 */

public class TCUpOpenSettingActivity extends Activity {
    public static final String RESULT_RECEIVER = "result_receiver";
    public static final String SETTING_ACTION = "setting_action";
    public static final String REQUEST_CODE = "request_code";
    private ResultReceiver resultReceiver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0,0);
        if(getIntent() != null) {
            resultReceiver = getIntent().getParcelableExtra(RESULT_RECEIVER);
            String settingAction = getIntent().getStringExtra(SETTING_ACTION);
            int requestCode = getIntent().getIntExtra(REQUEST_CODE, 0);
            Intent intent = new Intent(settingAction);
            startActivityForResult(intent,requestCode);
        }else {
            finish();
        }
    }


    private void onComplete(int requestCode) {
        Bundle bundle = new Bundle();
        resultReceiver.send(requestCode, bundle);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onComplete(requestCode);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }


    private void onPix(){
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

}
