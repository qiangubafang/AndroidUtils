package word_demo;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/6/15.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */
import android.app.Activity;
import android.content.Context;
import android.net.Uri;


import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.tcshare.app.amodule.activity.TCMainActivity;
import org.tcshare.poi.XWPFTemplate;
import org.tcshare.poi.config.Configure;
import org.tcshare.poi.data.PictureRenderData;
import org.tcshare.poi.plugin.table.LoopRowTableRenderPolicy;
import org.tcshare.poi.plugin.toc.TOCRenderPolicy;
import org.tcshare.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/21.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */
public class DebugTestWordTemplate {
    private static String outPath = null;

    public static void test(Activity ctx) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
                    System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
                    System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
                    ZipSecureFile.setMinInflateRatio(-1.0); // 隐藏的垃圾文件word/media/image1.png 压缩比，触发zip炸弹检测

                    Uri uriMain = Uri.parse("file:///android_asset-poi/word_template/temp_main.docx");

                    // 两个循环表格
                    LoopRowTableRenderPolicy loopRowTableRenderPolicy = new LoopRowTableRenderPolicy();
                    TOCRenderPolicy tocRenderPolicy = new TOCRenderPolicy();
                    Configure config = Configure.builder().bind("checkResultSum", loopRowTableRenderPolicy)
                            .bind("checkValue", loopRowTableRenderPolicy)
                            .bind("tocRender", tocRenderPolicy)
                            .build();

                    XWPFTemplate t = XWPFTemplate.compile(ctx, uriMain, config);

                    Report_220KV_ShengQian_2 datas = new Report_220KV_ShengQian_2();
                    datas.setImgDiaozhuang(DefaultPicLoadUtil.getImgDiaozhuang(ctx));
                    datas.setImgLuoChuan(DefaultPicLoadUtil.getImgLuoChuan(ctx));

                    datas.setImgPutDebug(DefaultPicLoadUtil.getImgPutDebug(ctx));
                    datas.setImgWorkCheck(DefaultPicLoadUtil.getImgWorkCheck(ctx));

                    // 机器人现场检测图片
                    List<PictureRenderData> imgCheckSites = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        imgCheckSites.add(DefaultPicLoadUtil.getImgCheckSite1(ctx));
                        imgCheckSites.add(DefaultPicLoadUtil.getImgCheckSite2(ctx));
                    }
                    datas.setImgCheckSite(imgCheckSites);

                    // 检查结果---->>>>>>>>>
                    int checkResultNum = 4;
                    List<Report_220KV_ShengQian_2.CheckResult> rowCheckResult = new ArrayList<>();
                    List<Report_220KV_ShengQian_2.CheckResultTable> rowCheckResultTable = new ArrayList<>();
                    List<Report_220KV_ShengQian_2.CheckResultTableSum> checkResultSum = new ArrayList<>();
                    for (int i = 1; i < checkResultNum; i++) {
                        Report_220KV_ShengQian_2.CheckResult row = new Report_220KV_ShengQian_2.CheckResult();

                        List<PictureRenderData> cr = new ArrayList<>();
                        cr.add(DefaultPicLoadUtil.getImgCheckResult1(ctx));
                        cr.add(DefaultPicLoadUtil.getImgCheckResult2(ctx));
                        row.setImgCheckResult(cr);

                        row.setCheckCount(i);
                        rowCheckResult.add(row);

                        Report_220KV_ShengQian_2.CheckResultTable crTable = new Report_220KV_ShengQian_2.CheckResultTable();
                        crTable.setCheckCount(i);

                        List<Report_220KV_ShengQian_2.CheckValue> checkValue = new ArrayList<>();
                        for(int count = 0; count < 10; count ++ ) {
                            Report_220KV_ShengQian_2.CheckValue cv = new Report_220KV_ShengQian_2.CheckValue(count + 1);
                            cv.setValue7("777");
                            cv.setValue8("888");
                            checkValue.add(cv);
                        }
                        crTable.setCheckValue(checkValue);
                        rowCheckResultTable.add(crTable);


                        Report_220KV_ShengQian_2.CheckResultTableSum crtSum = new Report_220KV_ShengQian_2.CheckResultTableSum();
                        crtSum.setCheckCount(i);
                        checkResultSum.add(crtSum);

                    }

                    // 检查结果； 多个
                    datas.setTemp_check_result(rowCheckResult);

                    // 检查结果表格 ---->>>>>>>>>
                    datas.setTemp_check_result_table(rowCheckResultTable);

                    // 检查结果汇总
                    datas.setCheckResultSum(checkResultSum);

                    t.render(datas);
                    File outFile = new File(ctx.getFilesDir(), "output.docx");
                    t.write(new FileOutputStream(outFile));
                    outPath = outFile.getPath();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToastLong(ctx, "替换word内容完成，检查logcat是否有错误输出！ 输出路径：" + outPath);
                        }
                    });
                }
            }
        }.start();

    }

}

