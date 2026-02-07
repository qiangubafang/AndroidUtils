package org.tcshare.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class MsgDialogUtil {

    public static void showMsg(Context ctx, String msg) {
        showMsgDialog(ctx, msg, false, (dialog, which) -> dialog.dismiss());
    }

    public static void showMsg(Context ctx, String msg, DialogInterface.OnClickListener ok) {
        showMsgDialog(ctx, msg, false, ok);

    }

    public static void showMsgHideStatusBar(Context ctx, String msg) {
        showMsgDialog(ctx, msg, true, (dialog, which) -> dialog.dismiss());
    }


    public static void showMsgHideStatusBar(Context ctx, String msg, DialogInterface.OnClickListener ok) {
        showMsgDialog(ctx, msg, true, ok);
    }


    public static void showMsgDialog(Context ctx, String msg, boolean hideStatusBar, DialogInterface.OnClickListener ok) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setTitle("信息")
                .setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (ok != null) {
            builder.setPositiveButton("确定", ok);
        }
        AlertDialog dialog = builder.create();
        if (hideStatusBar) {
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            dialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            //布局位于状态栏下方
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            //全屏
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            //隐藏导航栏
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    uiOptions |= 0x00001000;
                    dialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                }
            });
        }
        dialog.show();

    }


}
