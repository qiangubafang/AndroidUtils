package org.tcshare.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import org.tcshare.androidutils.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by FallRain on 2017/7/3.
 */

public class PackageUtil {
    public static boolean isAppInstalled(Context context, String appPackageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public static void startAPP(Context ctx) {
        startAPP(ctx, ctx.getPackageName());
    }

    public static void startAPP(Context ctx, String appPackageName) {
        try {
            Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(appPackageName);
            ctx.startActivity(intent);
        } catch (Exception e) {
            ToastUtil.showToastShort(ctx, ctx.getString(R.string.app_not_install));
        }
    }

    /**
     * 静默安装 使用命令： pm install
     * 需要 android:sharedUserId="android.uid.system"
     *
     * (1) pm install
     * pm install 命令的用法及参数解释如下:
     * pm install [-l][-r] [-t] [-i INSTALLER_PACKAGE_NAME] [-s] [-f] PATH
     * Options:
     * -l: install the package with FORWARD_LOCK.
     * -r: reinstall an exisiting app, keeping itsdata.
     * -t: allow test .apks to be installed.
     * -i: specify the installer package name.
     * -s: install package on sdcard.
     * -f: install package on internal flash.
     * (2) pm uninstall
     * pm uninstall 命令的用法及参数解释如下:
     * pm uninstall[-k] PACKAGE
     * Options:
     * -k: keep the data and cache directoriesaround.
     *
     * @param packageName 　 应用包名
     * @param apkFile    　　apk文件
     * @return  命令执行结果
     */
    public static ShellUtils.CommandResult installSilent(String packageName, String apkFile) {

        return ShellUtils.execCommand("pm install -i " + packageName + " -f " + apkFile, true, true);
    }


    /**
     *
     * @param ctx 从上下文获取包名
     * @param apkFile 文件位置
     * @return
     */
    public static ShellUtils.CommandResult installSilent(Context ctx, String apkFile) {

        return installSilent(ctx.getPackageName(), apkFile);
    }
    /**
     *
     * @param apkFile 文件位置
     * @return
     */
    public static ShellUtils.CommandResult updateSilent(String apkFile) {

        return ShellUtils.execCommand("pm install -r  -f " + apkFile, true, true);
    }

    public static ShellUtils.CommandResult uninstallSilent(String packageName) {

        return ShellUtils.execCommand("pm uninstall " + packageName, true, true);
    }

    /**
     * 使用am命令启动app
     * @param packageName
     * @param activity
     * @return
     */
    public static ShellUtils.CommandResult startApp(String packageName, String activity) {

        return ShellUtils.execCommand("am start -n " + packageName + "/" + activity, true, true);
    }

}
