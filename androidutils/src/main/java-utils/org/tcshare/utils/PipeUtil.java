package org.tcshare.utils;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/3/17.
 * Copyright (c) 2026 千古八方 All rights reserved.
 * 创建一个广岛
 */
public class PipeUtil {
    static {
        System.loadLibrary("qgbfutils");
    }
    public static native int createNamedPipe(String path);
}
