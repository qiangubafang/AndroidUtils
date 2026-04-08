package org.tcshare.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/3/12.
 * Copyright (c) 2026 千古八方 All rights reserved.
 * 
 * ---Activity 全屏后，Dialog show 会弹出底部的导航栏，使用以下方法隐藏导航栏------------------ 
 * 原理：使dialog失去焦点，就不会再弹出来导航栏了。再追加焦点，使dialog可用。
 * 1. 在dialog内部使用：
 *    @Override
 *     public void show() {
 *         Window window = getWindow();
 *         ActUtil.setNotFocusable(window);
 *         super.show();
 *         ActUtil.hideStatusBarAndNavBar(window);
 *         ActUtil.clearNotFocusable(window);
 *     }
 * 2. 已有的dialog使用
 *   Window window = calibrationDialog.getWindow();
 *   ActUtil.setNotFocusable(window);
 *   calibrationDialog.show();
 *   ActUtil.clearNotFocusable(window);
 *   ActUtil.hideStatusBarAndNavBar(window);
 */
public class ActUtil {
    /**
     * 隐藏状态栏和导航栏
     * @param window
     */
    public static void hideStatusBarAndNavBar(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                if (Build.VERSION.SDK_INT >= 19) {
                    uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                } else {
                    uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                }
                window.getDecorView().setSystemUiVisibility(uiOptions);
            }
        });
    }
    /**
     * 设置无法获取焦点
     *
     * @param window
     */
    public static void setNotFocusable(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /**
     * 清楚无法获取焦点标志
     *
     * @param window
     */
    public static void clearNotFocusable(Window window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
    
    /**
     * 屏幕常亮
     * @param window
     */
    public static void keepScreenOn(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 工控机设置。
     * 无系统权限，并且厂家未提供直接修改的API时，我们用标准写法。
     * @param window
     */
    public static void IndustrialControlStyle(Window window) {
        hideStatusBarAndNavBar(window);
        keepScreenOn(window);
    }


    /**
     * 获取当前屏幕方向
     * @param act
     * @return
     */
    public static int getCurrentOrientation(Activity act) {
        int rotation = act.getWindowManager().getDefaultDisplay().getRotation();
        if (act.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        } else {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
        }
    }

    /**
     * convert dp to its equivalent px
     */
    public static int dp2px(Context ctx, int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,ctx.getResources().getDisplayMetrics());
    }
    /**
     * convert px to its equivalent dp
     */
    public static int px2dp(Context ctx, int px){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px,ctx.getResources().getDisplayMetrics());
    }

    /**
     * convert sp to its equivalent px
     */
    public static int sp2px(Context ctx,int sp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,ctx.getResources().getDisplayMetrics());
    }

    public static DisplayMetrics calcScreenSize(Activity ctx) {
        DisplayMetrics dm = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
