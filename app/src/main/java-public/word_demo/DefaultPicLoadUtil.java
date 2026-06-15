package word_demo;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/6/15.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */

import android.content.Context;
import android.net.Uri;

import org.tcshare.poi.data.PictureRenderData;
import org.tcshare.poi.data.PictureType;
import org.tcshare.poi.data.UriPictureRenderData;


/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/21.
 * Copyright (c) 2026 千古八方 All rights reserved.
 *
 * 默认图片
 */
public  class DefaultPicLoadUtil {
    private static final String REPORT_NAME = "file:///android_asset-poi/word_template/";

    // 机器人现场检测图片1
    public  static PictureRenderData getImgCheckSite1(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "check_site1.jpg");
        return new UriPictureRenderData(ctx, uri, 240, 240, PictureType.JPEG);
    }

    // 机器人现场检测图片2
    public static PictureRenderData getImgCheckSite2(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "check_site2.jpg");
        return new UriPictureRenderData(ctx, uri, 240, 240, PictureType.JPEG);
    }

    // 检测结果图片1
    public static PictureRenderData getImgCheckResult1(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "check_result1.jpg");
        return new UriPictureRenderData(ctx, uri, 300, 300, PictureType.JPEG);
    }

    // 检测结果图片2
    public static PictureRenderData getImgCheckResult2(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "check_result2.jpg");
        return new UriPictureRenderData(ctx, uri, 300, 300, PictureType.JPEG);
    }




    // 无人机吊装机器人 ; 图片指定宽高
    public static PictureRenderData getImgDiaozhuang(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "diao_zhuang.jpg");
        return new UriPictureRenderData(ctx, uri, 500, 300, PictureType.JPEG);
    }

    // 机器人落串;
    public static PictureRenderData getImgLuoChuan(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "luo_chuan.jpg");
        return new UriPictureRenderData(ctx, uri, 500, 300, PictureType.JPEG);
    }

    // 机器人安放及调试;
    public static PictureRenderData getImgPutDebug(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "anfang_ceshi.jpg");
        return new UriPictureRenderData(ctx, uri, 500, 300, PictureType.JPEG);
    }

    // 机器人检测作业图片;
    public static PictureRenderData getImgWorkCheck(Context ctx) {
        Uri uri = Uri.parse(REPORT_NAME + "jiance_zuoye.jpg");
        return new UriPictureRenderData(ctx, uri, 500, 300, PictureType.JPEG);
    }
}

