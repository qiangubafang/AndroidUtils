package org.tcshare.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tcshare.androidutils.R;
import org.tcshare.utils.ActUtil;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/8.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */
public class LoadingDialog extends Dialog {
    private final String msg;
    private final Animation animation;
    private final boolean immerse;
    private ImageView img;

    public LoadingDialog(@NonNull Context context, @Nullable String msg, boolean immerse) {
        super(context, R.style.loading_dialog);
        this.immerse = immerse;
        this.msg = msg;
        animation = AnimationUtils.loadAnimation(context, R.anim.dialog_loading);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_loading);
        LinearLayout layout = findViewById(R.id.dialog_view);
        img = findViewById(R.id.img);
        TextView tipText = findViewById(R.id.tipTextView);


        if (!TextUtils.isEmpty(msg)) {
            tipText.setVisibility(View.VISIBLE);
            tipText.setText(msg);
        } else {
            tipText.setVisibility(View.GONE);
        }

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        img.startAnimation(animation);
    }

    @Override
    public void show() {
        if (immerse){
            Window window = getWindow();
            ActUtil.setNotFocusable(window);
            super.show();
            ActUtil.hideStatusBarAndNavBar(window);
            ActUtil.clearNotFocusable(window);
        }else {
            super.show();
        }

    }

    @Override
    public void dismiss() {
        Animation anim = img.getAnimation();
        if (anim != null) {
            anim.cancel();
        }
        super.dismiss();
    }

    public static Dialog createLoadingDialog(final Context context, @Nullable String msg) {
        return createLoadingDialog(context, msg, false);
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @param immerse
     * @return
     */
    public static Dialog createLoadingDialog(final Context context, @Nullable String msg, boolean immerse) {

        return new LoadingDialog(context,msg, immerse);
    }
}
