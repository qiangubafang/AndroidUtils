package org.tcshare.poi.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/23.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */
public class AndroidUriUtil {


    /**
     * @Description TODO
     * <p>
     * Created by 千古八方 on 2026/4/22.
     * Copyright (c) 2026 千古八方 All rights reserved.
     */
    public static final String ANDROID_ASSETS = "android_asset";

    public static InputStream openInputStream(Context ctx, Uri uri) throws IOException {
        if (ctx == null || uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (path == null) {
            return null;
        }

        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            if (path.contains(ANDROID_ASSETS)) {
                int index = path.indexOf(ANDROID_ASSETS);
                String assetPath = path.substring(index).substring(path.substring(index).indexOf("/") + 1);

                return ctx.getAssets().open(assetPath);
            } else {
                return new FileInputStream(uri.getPath());
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return ctx.getContentResolver().openInputStream(uri);
        } else if ("android.resource".equals(scheme)) {
            return ctx.getContentResolver().openInputStream(uri);
        }
        return null;

    }

    public static byte[] readAllBytes(Context ctx, Uri uri) throws IOException {
        if (ctx == null || uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (path == null) {
            return null;
        }
        InputStream inputStream = null;

        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            if (path.startsWith(ANDROID_ASSETS)) {
                inputStream = ctx.getAssets().open(path.substring(ANDROID_ASSETS.length()));
            } else {
                inputStream =  new FileInputStream(uri.getPath());
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            inputStream =  ctx.getContentResolver().openInputStream(uri);
        } else if ("android.resource".equals(scheme)) {
            inputStream =  ctx.getContentResolver().openInputStream(uri);
        }
        if(inputStream != null){
            try {
                return readAllBytes(inputStream);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                inputStream.close();
            }
        }

        return null;

    }

    /**
     * 这里没关流，外部关闭
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] readAllBytes(InputStream in) throws IOException {
        BufferedInputStream br = new BufferedInputStream(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int len = -1;
        while ((len = br.read(buffer)) != -1){
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

}
