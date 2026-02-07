package org.tcshare.permission;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import org.tcshare.activity.TCRequestPermissionActivity;
import org.tcshare.activity.TCUpOpenSettingActivity;

import java.util.Arrays;
import java.util.Observable;

/**
 * Created by FallRain on 2017/8/18.
 */

public class SettingHelper extends Observable {

    public static void request(Context ctx, String action, int requestCode, final Callback callback) {
        Intent intent = new Intent();
        intent.putExtra(TCUpOpenSettingActivity.SETTING_ACTION, action);
        intent.putExtra(TCUpOpenSettingActivity.REQUEST_CODE, requestCode);
        intent.putExtra(TCUpOpenSettingActivity.RESULT_RECEIVER, new ResultReceiver(new Handler(Looper.getMainLooper())) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                callback.onResult(requestCode, action, resultCode);
            }
        });
        intent.setClass(ctx, TCUpOpenSettingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        ctx.startActivity(intent);
    }


    public interface Callback {
        void onResult(int requestCode, String action, int resultCode);
    }
}
