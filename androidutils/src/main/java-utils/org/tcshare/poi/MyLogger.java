package org.tcshare.poi;


import android.util.Log;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/23.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */
public class MyLogger {

    private final String tag;

    private MyLogger(String tag) {
        this.tag = tag;
    }

    public static MyLogger getLogger(Class<?> cls) {
        return new MyLogger(cls.getSimpleName());
    }

    public void debug(String format, Object... data) {
        Log.d(tag, String.format(format, data));
    }

    public void info(String format, Object... data) {
        Log.i(tag, String.format(format, data));
    }

    public void warn(String format, Object... data) {
        Log.w(tag, String.format(format, data));
    }
    public void error(String format, Object... data) {
        Log.e(tag, String.format(format, data));
    }
}
