package org.tcshare.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 *  // 监听安装包的广播，用于安装完成后自动启动自身
 *  // 静听注册
 *          <receiver android:name="com.example.testfile.BootBroadcastReceiver" >
 *             <intent-filter>
 *                 <action android:name="android.intent.action.PACKAGE_ADDED" />
 *                 <action android:name="android.intent.action.PACKAGE_REPLACED" />
 *                 <action android:name="android.intent.action.PACKAGE_REMOVED" />
 *                 <data android:scheme="package" />
 *             </intent-filter>
 *         </receiver>
 *
 *  // 动态注册自定义广播接收器
 *         receiver = new BootBroadcastReceiver();
 *         IntentFilter filter = new IntentFilter();
 *         filter.addAction(Intent.ACTION_PACKAGE_ADDED);
 *         filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
 *         filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
 *         registerReceiver(receiver, filter);
 */
public abstract class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = BootBroadcastReceiver.class.getSimpleName();

    /**
     * 需要检测的包名
     * @return
     */
    public abstract List<String> getPackageNames();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            Log.v(TAG, "BootBroadcastReceiver packageName:" + packageName);
            if (getPackageNames().contains(packageName)) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Log.v(TAG, "Auto launch :" + packageName);
            }
        }

    }
}
