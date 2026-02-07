package org.tcshare.app;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.google.gson.Gson;

import org.tcshare.broadcast.BootBroadcastReceiver;
import org.tcshare.permission.PermissionHelper;
import org.tcshare.utils.PackageUtil;
import org.tcshare.utils.PhoneInfo;
import org.tcshare.utils.ShellUtils;
import org.tcshare.utils.UnZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xcrash.XCrash;

/**
 * Created by FallRain on 2017/8/21.
 */

public class App extends Application {
    private static final boolean TEST = false;
    public static Context ctx;
    private static final String ROOT_DIR = Environment.getExternalStorageDirectory() + File.separator + "es_tools";
    private static final File CRASH_DIR =  new File(ROOT_DIR + File.separator + "crash/");;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        if (TEST) {
            runTest();
        }

        Map<String, String> map = PhoneInfo.getCpuInfo();
        Log.e("DEBUG", "map:" + map);
    }

    private void runTest() {

        // 测试php
        new Thread() {
            @Override
            public void run() {
                super.run();
                File serverDir = new File(getFilesDir(), "server");
                String path = "/data/data/" + getPackageName() + "/files/server";
                String cmdBusybox = path + "/busybox ";
                String cmdPhp = path + "/php ";
                String cmdStart = path + "/start.sh ";
                String cmdStop = path + "/stop.sh ";

                if (!serverDir.isDirectory()) {
                    try {
                        if (UnZipUtil.unZip(getAssets().open("server.zip"), getFilesDir().getPath())) {
                            ShellUtils.CommandResult chmodRet = ShellUtils.execCommand(new String[]{
                                    "chmod 755 -R " + path,
                                    "chmod 755 " + cmdStart,
                                    "chmod 755 " + cmdStop,
                                    "chmod 755 " + cmdBusybox,
                                    "chmod 755 " + cmdPhp}, true);
                            Log.e("TAG", "chmod: " + chmodRet.toString());
                        } else {
                            Log.e("TAG", "解压失败！");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("TAG", "打开压缩文件失败！" + e.getMessage());
                    }

                }

                ShellUtils.CommandResult startServerRet = ShellUtils.execCommand(new String[]{cmdStop + path, cmdStart + path}, true);
                Log.e("TAG", "startServer:" + startServerRet.toString());
            }
        }.start();

        // 静默启动无法已给屏蔽
        // 需要监听应用安装的事件
        BootBroadcastReceiver receiver = new BootBroadcastReceiver() {

            @Override
            public List<String> getPackageNames() {
                return new ArrayList<String>() {
                    {
                        add(ctx.getPackageName());
                    }
                };
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(receiver, filter);
        // 3288 专有的。
        showNavStatusBar(true);

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(5_000);
                    PermissionHelper.request(ctx, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 9999, new PermissionHelper.Callback() {

                        @Override
                        public void onResult(int requestCode, String[] permissions, int[] grantResult) {
                            ShellUtils.CommandResult ret = PackageUtil.updateSilent(Environment.getExternalStorageDirectory() + "/test.apk");
                            Log.e("TAG", "test slient install apk :" + new Gson().toJson(ret));
                            PackageUtil.startAPP(ctx);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // rockchip rk3288 上使用的，这里用于测试，不通用。
    public void showNavStatusBar(boolean show) {
        // 显示|隐藏状态栏，导航栏
        Intent systemBarIntent = new Intent("com.tchip.changeBarHideStatus");
        Intent statusBarIntent = new Intent("com.tchip.changeStatusBarHideStatus");
        String showNavCmd = "settings put system systembar_hide " + (show ? "0" : "1");
        String showStatusBar = "settings put system systemstatusbar_hide " + (show ? "0" : "1");//1隐藏状态栏，0显示状态栏
        ShellUtils.execCommand(showNavCmd, true);
        ShellUtils.execCommand(showStatusBar, true);
        sendBroadcast(systemBarIntent);
        sendBroadcast(statusBarIntent);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);


        // 检查路径是否存在
        checkDir();

        //Tombstone 文件默认将被写入到
        XCrash.InitParameters crashParams = new XCrash.InitParameters();
        crashParams.setLogDir(CRASH_DIR.getPath());
        xcrash.XCrash.init(this, crashParams); //崩溃捕获
    }

    private void checkDir() {
        if (!CRASH_DIR.exists()) {
            CRASH_DIR.mkdirs();
        }
    }

}
