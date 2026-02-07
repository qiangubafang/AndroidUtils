package org.tcshare.utils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import org.tcshare.activity.TCUpOpenSettingActivity;
import org.tcshare.androidutils.R;
import org.tcshare.permission.SettingHelper;

import java.io.File;

/**
 * Created by dell on 2017/4/7.
 * 8.0 适配需要以下权限，下载到公共download目录下
 * <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>
 */
public class UpdateUtil {
    private static final String TAG = UpdateUtil.class.getSimpleName();
    private final Context ctx;
    private final String downURL;
    private final String notifyTitle;
    private final String notifyDesc;
    private final String apkName;
    private File apkFile;
    private DownloadManager mDownloadManager;
    private long downloadId;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };

    public UpdateUtil(Context ctx, String downURL, String apkName, @Nullable String notifyTitle, @Nullable String notifyDesc) {
        this.ctx = ctx;
        this.apkName = apkName;
        this.downURL = downURL;
        this.notifyTitle = notifyTitle;
        this.notifyDesc = notifyDesc;
    }

    public void update() {

        final String packageName = "com.android.providers.downloads";
        int state = ctx.getPackageManager().getApplicationEnabledSetting(packageName);
        //检测下载管理器是否被禁用
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx).setTitle(R.string.info).setMessage
                    (R.string.warn_download_manager_disable).setPositiveButton(R.string.ok, (dialog, which) -> {
                dialog.dismiss();
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + packageName));
                    ctx.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    ctx.startActivity(intent);
                }
            }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else {
            //正常下载流程
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downURL));
            request.setAllowedOverRoaming(true);
            //通知栏显示
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle(TextUtils.isEmpty(notifyTitle) ? ctx.getString(R.string.download) : notifyTitle);
            request.setDescription(TextUtils.isEmpty(notifyDesc) ? ctx.getString(R.string.downloading) : notifyDesc);
            request.setVisibleInDownloadsUi(true);

            apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName);
            //设置下载的路径
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
            //获取DownloadManager
            mDownloadManager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            ctx.registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            downloadId = mDownloadManager.enqueue(request);
        }
    }

    /**
     * 检查下载状态
     */
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    Log.e(TAG, "DownloadManager.STATUS_PAUSED");
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    Log.e(TAG, "DownloadManager.STATUS_PENDING");
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    Log.e(TAG, "DownloadManager.STATUS_RUNNING");
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    //8.0兼容 安装apk权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!ctx.getPackageManager().canRequestPackageInstalls()) {
                            new AlertDialog.Builder(ctx).setTitle("设置")
                                    .setMessage("请打开允许当前APP安装apk的设置，否则无法安装，是否去设置？")
                                    .setPositiveButton("去设置", (dialog, which) -> requestSettings())
                                    .setNegativeButton("放弃安装", (dialog, which) -> dialog.dismiss())
                                    .show();
                            return;
                        }
                    }
                    installAPK();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Log.e(TAG, "DownloadManager.STATUS_FAILED");
                    ToastUtil.showToastShort(ctx, R.string.download_failed);
                    break;
            }
        }
        cursor.close();
    }

    // 8.0 兼容
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestSettings() {
        SettingHelper.request(ctx, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, 5869, new SettingHelper.Callback() {
            @Override
            public void onResult(int requestCode, String action, int resultCode) {
                if (ctx.getPackageManager().canRequestPackageInstalls()) {
                    installAPK();
                } else {
                    new AlertDialog.Builder(ctx).setTitle("设置")
                            .setMessage("请打开允许当前APP安装apk文件的设置，否则无法安装，是否去设置？")
                            .setPositiveButton("去设置", (dialog, which) -> requestSettings())
                            .setNegativeButton("放弃安装", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }
        });
    }


    /**
     * 7.0兼容
     */
    private void installAPK() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        ctx.startActivity(intent);
    }

}