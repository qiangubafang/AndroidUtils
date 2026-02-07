package org.tcshare.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.tcshare.androidutils.R;


/**
 * author:于小嘿
 * date: 2020/7/21
 * desc:
 */
public class ToastUtil {

    public static void showToastLong(Context context, String msg) {
        showToast(context, msg, Toast.LENGTH_LONG);
    }

    public static void showToastLong(Context context, int msgID) {
        showToast(context, context.getResources().getString(msgID), Toast.LENGTH_LONG);
    }

    public static void showToastShort(Context context, String msg) {
        showToast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void showToastShort(Context context, int msgID) {
        showToast(context, context.getResources().getString(msgID), Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Context context, String msg, @Nullable Integer bgColor, int fSize, @Nullable Integer fColor, int w, int h) {
        showToast(context, msg, Toast.LENGTH_LONG, bgColor, fSize, fColor, w, h);
    }

    public static void showToastLong(Context context, int msgID, @Nullable Integer bgColor, int fSize, @Nullable Integer fColor, int w, int h) {
        showToast(context, context.getResources().getString(msgID), Toast.LENGTH_LONG, bgColor, fSize, fColor, w, h);
    }

    public static void showToastShort(Context context, String msg, @Nullable Integer bgColor, int fSize, @Nullable Integer fColor, int w, int h) {
        showToast(context, msg, Toast.LENGTH_SHORT, bgColor, fSize, fColor, w, h);
    }

    public static void showToastShort(Context context, int msgID, @Nullable Integer bgColor, int fSize, @Nullable Integer fColor, int w, int h) {
        showToast(context, context.getResources().getString(msgID), Toast.LENGTH_SHORT, bgColor, fSize, fColor, w, h);
    }

    private static Toast toast;

    private static void showToast(Context context, String msg, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, duration);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.bg_toast);
            TextView message = ((TextView) view.findViewById(android.R.id.message));
            message.setTextColor(Color.WHITE);
            message.setGravity(Gravity.CENTER);
            message.setPadding(DensityUtil.dp2px(context, 10), DensityUtil.dp2px(context, 6), DensityUtil.dp2px(context, 10), DensityUtil.dp2px(context, 6));
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            //TODO 自定义一个toast android 11, api31 后不允许自定义toast
        }
        toast.show();
    }


    private static void showToast(Context context, String msg, int duration, @Nullable Integer bgColor, int fSize, @Nullable Integer fColor, int w, int h) {
        Toast toast = Toast.makeText(context, msg, duration);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            View view = toast.getView();
            view.setBackgroundResource(R.drawable.bg_toast);
            if (bgColor != null) {
                view.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            }
            TextView message = ((TextView) view.findViewById(android.R.id.message));
            if (fColor != null) {
                message.setTextColor(fColor);
            } else {
                message.setTextColor(Color.WHITE);
            }
            message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fSize);

            ViewGroup.LayoutParams layoutParams = message.getLayoutParams();
            layoutParams.width = w;
            layoutParams.height = h;
            message.setLayoutParams(layoutParams);
            message.setGravity(Gravity.CENTER);
            message.setPadding(DensityUtil.dp2px(context, 10), DensityUtil.dp2px(context, 6), DensityUtil.dp2px(context, 10), DensityUtil.dp2px(context, 6));
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            //TODO 自定义一个toast android 11, api31 后不允许自定义toast
        }
        toast.show();
    }


}
