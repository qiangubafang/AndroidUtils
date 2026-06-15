package org.tcshare.poi.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;

import org.tcshare.poi.util.AndroidUriUtil;

import java.io.ByteArrayOutputStream;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/23.
 * Copyright (c) 2026 千古八方 All rights reserved.
 *
 * 我们重新缩放，并且从Android 的URI读取
 */
public class UriPictureRenderData extends PictureRenderData {

    private final Context ctx;
    private int height = -1;
    private int width = -1;
    private final Uri uri;

    public UriPictureRenderData(Context ctx, Uri uri, int width, int height, PictureType pictureType) {
        this.ctx = ctx;
        this.uri = uri;
        this.width = width;
        this.height = height;
        this.pictureType = pictureType;

    }
    public UriPictureRenderData(Context ctx, Uri uri,  PictureType pictureType) {
        this.ctx = ctx;
        this.uri = uri;
        this.pictureType = pictureType;

    }

    @Override
    public byte[] readPictureData() {
        try {
            byte[] bytes = AndroidUriUtil.readAllBytes(ctx, uri);
            if(width > 0 && height > 0) {
                if (bytes != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap newBitmap = scaleBitmap(bitmap, width, height);
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 95, bos);

                    return bos.toByteArray();
                }
            }else{
                return bytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * 等比例缩小 Bitmap，限制最大宽高，不拉伸，返回可变 Bitmap
     * @param srcBitmap 原始图片
     * @param maxWidth 限制最大宽度
     * @param maxHeight 限制最大高度
     * @return 缩放后新 Bitmap（mutable 可变）
     */
    public static Bitmap scaleBitmap(Bitmap srcBitmap, int maxWidth, int maxHeight) {
        int srcW = srcBitmap.getWidth();
        int srcH = srcBitmap.getHeight();

        // 原图本身更小，直接返回副本（可变）
        if (srcW <= maxWidth && srcH <= maxHeight) {
            return srcBitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        // 计算宽高缩放比例
        float scaleWidth = (float) maxWidth / srcW;
        float scaleHeight = (float) maxHeight / srcH;
        // 取较小比例，保证宽高都不超过限制
        float scale = Math.min(scaleWidth, scaleHeight);

        // 新尺寸
        int newW = Math.round(srcW * scale);
        int newH = Math.round(srcH * scale);

        return Bitmap.createScaledBitmap(srcBitmap, newW, newH, false);
    }
}
