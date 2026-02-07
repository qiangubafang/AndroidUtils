package org.tcshare.logutils;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyAndroidLogAdapter {
    private static final int MAX_BYTES = 5_000 * 1024; // 500K averages to a 4000 lines per file
    private static final int DEFAULT_KEEP_DAYS = 90; // 默认保留90天
    private static MyAndroidLogAdapter instance;
    private String folder;
    private  Timer timer;

    private MyAndroidLogAdapter() {
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        folder = diskPath + File.separatorChar + "logger";
        init(folder);

    }

    public void init(String folder, int keeDays){
        this.folder = folder;
        HandlerThread ht = new HandlerThread("AndroidFileLogger." + folder);
        ht.start();
        Handler handler = new MyDiskLogStrategy.WriteHandler(ht.getLooper(), folder, MAX_BYTES);
        MyDiskLogStrategy logStrategy = new MyDiskLogStrategy(handler);
        PrettyFormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder().logStrategy(logStrategy).build();

        Logger.clearLogAdapters();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        deleteOverDateFile(new File(folder), keeDays < 0 ? DEFAULT_KEEP_DAYS : keeDays); // 每天删除一次，日志最多保留多久
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e(new Date() + "删除日志时异常：" + e.getMessage());
                    }
                }
            }, 1_0000, 24 * 60 * 60 * 1000);
        }catch (Exception e){
            e.printStackTrace();
            Logger.e("定时删除日志错误。" + e.getMessage());
        }
    }

    public void init(String folder) {
        init(folder, DEFAULT_KEEP_DAYS);
    }

    public static MyAndroidLogAdapter getInstance() {
        synchronized (MyAndroidLogAdapter.class) {
            if (instance == null) {
                instance = new MyAndroidLogAdapter();
            }
        }
        return instance;
    }

    public static void deleteOverDateFile(File dirPath, int keepDays) {
        File[] files = dirPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                try {
                    Date currentDate = new Date(System.currentTimeMillis());
                    Date lastModifyDate = new Date(file.lastModified());
                    long diff = currentDate.getTime() - lastModifyDate.getTime(); // 毫秒
                    long days = diff / (24 * 60 * 60 * 1000);
                    return days > keepDays;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }
        });
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

}
