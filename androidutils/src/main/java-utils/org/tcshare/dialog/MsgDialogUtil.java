package org.tcshare.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;

import org.tcshare.utils.ActUtil;

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
            Window window = dialog.getWindow();
            ActUtil.setNotFocusable(window);
            dialog.show();
            ActUtil.clearNotFocusable(window);
            ActUtil.hideStatusBarAndNavBar(window);
        }else {
            dialog.show();
        }

    }


}
