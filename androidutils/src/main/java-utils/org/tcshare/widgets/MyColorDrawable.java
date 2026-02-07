package org.tcshare.widgets;

import android.graphics.drawable.ColorDrawable;

public class MyColorDrawable extends ColorDrawable {
    private final int w,h;

    public MyColorDrawable(int w, int h, int color){
        super(color);
        this.w = w;
        this.h = h;
    }
    @Override
    public int getIntrinsicWidth() {
        return w;
    }

    @Override
    public int getIntrinsicHeight() {
        return h;
    }
}
