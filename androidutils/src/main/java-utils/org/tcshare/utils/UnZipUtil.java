package org.tcshare.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author 于小嘿
 */
public class UnZipUtil {
    public static boolean unZip(String inFile, String outPath) {
        boolean ret = false;
        try {
            ret = unZip(new FileInputStream(inFile), outPath, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean unZip(InputStream inFile, String outPath) {
        return unZip(inFile, outPath, null);
    }

    public static boolean unZip(InputStream inputStream, String outPath, ProgressListener listener) {
        boolean isSuccess = false;
        ZipInputStream zipInputStream = null;
        createDirectory(outPath, "");
        zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        try {
            FileOutputStream fout;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    createDirectory(outPath, zipEntry.getName());
                } else {
                    fout = new FileOutputStream(outPath + "/" + zipEntry.getName());
                    if (listener != null) {
                        listener.onProgress(false, true, zipEntry.getName());
                    }
                    byte[] buffer = new byte[4096 * 10];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        fout.write(buffer, 0, length);
                    }
                    zipInputStream.closeEntry();
                    fout.close();
                }
            }
            zipInputStream.close();
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onProgress(true, isSuccess, isSuccess ? "success!" : "failed!");
        }
        return isSuccess;
    }


    protected static void createDirectory(String outPath, String dirName) {
        File file = new File(outPath, dirName);
        if (!file.isDirectory()) file.mkdirs();
    }

    public interface ProgressListener {
        void onProgress(boolean isFinished, boolean success, String msg);
    }
}
