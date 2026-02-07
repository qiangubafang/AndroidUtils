package org.tcshare.app.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;

import org.tcshare.app.network.Net;
import org.tcshare.app.network.entity.ResUpdateCheckBean;
import org.tcshare.network.HttpApi;
import org.tcshare.network.ResponseJSON;
import org.tcshare.utils.ToastUtil;
import org.tcshare.utils.UpdateUtil;

import okhttp3.Call;

public class CheckUpdateTask {
    // demo
    public static void checkUpdate(Activity act, boolean showIsLast) {
        HttpApi.get(Net.UPDATE_CHECK, new ResponseJSON<ResUpdateCheckBean>(act) {
            @Override
            public void onResponseUI(Call call, ResUpdateCheckBean processObj) {
                if (processObj != null && "0".equals(processObj.getCode())) {
                    try {
                        PackageInfo info = act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
                        int labelRes = info.applicationInfo.labelRes;
                        String appName = ctx.getResources().getString(labelRes);
                        ResUpdateCheckBean.DataBean data = processObj.getData().get(0);
                        /*
                        // 使用版本名称比较
                        Version local = new Version(info.versionName);
                        Version net = new Version(data.getVersion());
                        if (local.compareTo(net) < 0) {}*/
                        if (info.versionCode < Integer.parseInt(data.getVersion())) {
                            new AlertDialog.Builder(act)
                                    .setTitle("升级")
                                    .setMessage("检测到新版本 v" + data.getVersion() + "，是否升级？")
                                    .setPositiveButton("确定", (dialog, which) -> {
                                        ToastUtil.showToastLong(act, "程序下载中，完成后将提示您进行安装!");

                                        new UpdateUtil(act, data.getDownloadpath(), "试验信息系统" + data.getVersion() + ".apk", appName, "APP升级").update();
                                        dialog.dismiss();
                                    }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();


                        } else if (showIsLast) {
                            ToastUtil.showToastLong(act, "当前版本已是最新版本！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
