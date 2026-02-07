package org.tcshare.fileselectorlib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.tcshare.activity.TCFileSelectorActivity;
import org.tcshare.fileselectorlib.Objects.BasicParams;
import org.tcshare.fileselectorlib.Objects.FileInfo;
import org.tcshare.fileselectorlib.Utils.PermissionUtil;
import org.tcshare.permission.PermissionHelper;
import org.tcshare.utils.PackageUtil;
import org.tcshare.utils.ShellUtils;
import org.tcshare.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class FileSelectorSettings {

    private BasicParams basicParams;
    public static String FILE_PATH_LIST_REQUEST = "file_path_list";
    public static int BACK_WITHOUT_SELECT = 510;
    public static int BACK_WITH_SELECTIONS = 511;
    public static int FILE_LIST_REQUEST_CODE = 512;

    public FileSelectorSettings() {
        basicParams=BasicParams.getInitInstance();
    }

    public FileSelectorSettings setRootPath(String path){
        basicParams.setRootPath(path);
        return this;
    }

    public FileSelectorSettings setMaxFileSelect(int num){
        basicParams.setMaxSelectNum(num);
        return this;
    }

    public FileSelectorSettings setTitle(String title){
        basicParams.setTips(title);
        return this;
    }

    public FileSelectorSettings setTheme(FileSelectorTheme theme){
        basicParams.setTheme(theme);
        return this;
    }

    public FileSelectorSettings setFileTypesToSelect(FileInfo.FileType ... fileTypes){
        if (Arrays.asList(fileTypes).contains(FileInfo.FileType.Parent)){
            throw new IllegalArgumentException("类型不能包含parent");
        }
        else basicParams.setSelectableFileTypes(fileTypes);
        return this;
    }
    public FileSelectorSettings setFileTypesToShow(String ... extensions){
        basicParams.setFileTypeFilter(extensions);//如果extensions 为空,则只显示文件夹
        basicParams.setUseFilter(true);
        return this;
    }
    public FileSelectorSettings setCustomizedIcons(String[] extensions, Bitmap ... icons){
        if (extensions.length!=icons.length){
            throw new IllegalArgumentException("文件扩展名必须与自定义图标一一对应");
        }
        else {
            for (int i = 0; i < extensions.length; i++) {
                basicParams.addCustomIcon(extensions[i],icons[i]);
            }
        }
        return this;
    }

    public FileSelectorSettings setCustomizedIcons(String[] extensions, Context context, int ... icon_ids){
        if (extensions.length!=icon_ids.length){
            throw new IllegalArgumentException("文件扩展名必须与自定义图标一一对应");
        }
        else {
            for (int i = 0; i < extensions.length; i++) {
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), icon_ids[i]);
                basicParams.addCustomIcon(extensions[i],icon);
            }
        }
        return this;
    }

    public FileSelectorSettings setMoreOptions(String[] optionsName, BasicParams.OnOptionClick...onOptionClicks){
        if (optionsName.length!=onOptionClicks.length){
            throw new IllegalArgumentException("选项名和点击响应必须一一对应");
        }
        else {
            basicParams.setNeedMoreOptions(true);
            basicParams.setOptionsName(optionsName);
            basicParams.setOnOptionClicks(onOptionClicks);
        }
        return this;
    }

    /**
     * 获取系统根目录
     * @return /storage/emulated/0
     */
    public static String getSystemRootPath(){
        return BasicParams.BasicPath;
    }

    public FileSelectorSettings setFileListRequestCode(int fileListRequestCode) {
        FILE_LIST_REQUEST_CODE = fileListRequestCode;
        return this;
    }

    public static boolean checkPermision(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                return true;
            } else {
                Toast.makeText(ctx, "Android 版本11之后，需要选择文件权限!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + ctx.getPackageName()));
                ctx.startActivity(intent);
                return false;
            }
        }
        return true;
    }

    public  void show(final Context ctx, final CallBack callBack) {
        if (!checkPermision(ctx)) {
            return;
        }
        PermissionHelper.request(ctx, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILE_LIST_REQUEST_CODE, new PermissionHelper.Callback() {

            @Override
            public void onResult(int requestCode, String[] permissions, int[] grantResult) {
                boolean granted = true;
                for(int grant : grantResult) {
                    if (PackageManager.PERMISSION_GRANTED != grant) {
                        granted = false;
                        break;
                    }
                }
                if(!granted) {
                    ToastUtil.showToastShort(ctx, "请授予读写文件的权限！");
                }else{
                    Intent intent = new Intent();
                    intent.putExtra(TCFileSelectorActivity.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            ArrayList<String> filePathSelected = new ArrayList<>();
                            if (resultCode == FileSelectorSettings.BACK_WITH_SELECTIONS) {
                                ArrayList<String> files = resultData.getStringArrayList(FileSelectorSettings.FILE_PATH_LIST_REQUEST);
                                if(files != null) {
                                    filePathSelected.addAll(files);
                                }
                            }
                            callBack.onResult(resultCode, filePathSelected);
                        }
                    });
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.setClass(ctx, TCFileSelectorActivity.class);
                    ctx.startActivity(intent);
                }
            }
        });

    }

    public interface CallBack {
        void onResult(int resultCode, ArrayList<String> pathList);
    }
}
