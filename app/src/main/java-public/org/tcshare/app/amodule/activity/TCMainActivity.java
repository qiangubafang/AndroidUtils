package org.tcshare.app.amodule.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.tcshare.app.R;
import org.tcshare.app.amodule.fragment.EmptyFragment;
import org.tcshare.app.amodule.fragment.RVListFragment;
import org.tcshare.app.network.entity.TmpTest;
import org.tcshare.app.zxing.CaptureActivity;
import org.tcshare.dialog.LoadingDialog;
import org.tcshare.fileselectorlib.FileSelectorSettings;
import org.tcshare.fileselectorlib.Objects.FileInfo;
import org.tcshare.fragment.WebViewFragment;
import org.tcshare.logutils.HttpLogger;
import org.tcshare.logutils.MyAndroidLogAdapter;
import org.tcshare.logview.logcatviewer.utils.LogcatViewer;
import org.tcshare.network.HttpApi;
import org.tcshare.network.HttpLogInterceptor;
import org.tcshare.network.ResponseString;
import org.tcshare.network.cookie.CookieJarImpl;
import org.tcshare.network.cookie.PersistentCookieStore;
import org.tcshare.permission.PermissionHelper;
import org.tcshare.utils.HexDump;
import org.tcshare.utils.Notify;
import org.tcshare.utils.ToastUtil;
import org.tcshare.utils.UpdateUtil;
import org.tcshare.utils.bluetooth.BTUtil;
import org.tcshare.utils.crc.CRCModel;
import org.tcshare.utils.crc.bitwise.BitwiseBigCRC;
import org.tcshare.utils.crc.bitwise.BitwiseCRC;
import org.tcshare.utils.crc.table_driven.TableDrivenCRC;
import org.tcshare.utils.ntp.UpdateSystemTimeUtil;
import org.tcshare.utils.packet.PacketUtil;
import org.tcshare.utils.rs485serial.RS485SerialPortUtilNew;
import org.tcshare.utils.rs485serial.RS485SerialPortUtilNewReceiveOnly;
import org.tcshare.utils.websocket.IReceiveMessage;
import org.tcshare.utils.websocket.WebSocketManager;
import org.tcshare.widgets.BottomListDialog;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class TCMainActivity extends AppCompatActivity {

    private static final String TAG = TCMainActivity.class.getSimpleName();
    private EditText serialPortText;
    private EditText serialPortBand;

    private Dialog loadingDialog;
    private BTUtil btUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc_main);
        initDiskLogger();
        serialPortText = findViewById(R.id.serialPortText);
        serialPortBand = findViewById(R.id.serialPortBand);

        PacketUtil packetUtil = new PacketUtil((byte) 0x68, (byte) 0x55, (byte) 0x16, 512);
        btUtil = new BTUtil(null,
                "0000fff1-0000-1000-8000-00805f9b34fb",
                "0000fff2-0000-1000-8000-00805f9b34fb",
                1000,
                packetUtil);
        btUtil.setDEBUG(true);


        loadingDialog = LoadingDialog.createLoadingDialog(this, "测试", false);
        testRS485_1_2();


        RS485SerialPortUtilNewReceiveOnly rsp = new RS485SerialPortUtilNewReceiveOnly(10);
        rsp.open(9600, "/dev/ttyS0", null, false);
        Log.e("KK", "open ret:" + rsp.isOpenSerialSuccess());
        rsp.setOnReceiveCallback(new RS485SerialPortUtilNewReceiveOnly.OnReceiveCallback() {
            @Override
            public void onReceive(byte[] data) {
                Log.e("MM", "recv:" + HexDump.toHexString(data));
            }
        });

        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.e("TAG", System.currentTimeMillis() + "");
                Pair<Boolean, Long> ret = UpdateSystemTimeUtil.updateSystem("cn.ntp.org.cn");
                Log.e("TAG", ret + "");
            }
        }.start();

/*
        WebSocketManager.getInstance().init("ws://39.91.86.71:20000", 10_000, new IReceiveMessage() {
            @Override
            public void onConnectSuccess() {
                Log.e(TAG, "onConnectSuccess");
            }

            @Override
            public void onConnectFailed() {
                Log.e(TAG, "onConnectFailed");
            }

            @Override
            public void onClose() {
                Log.e(TAG, "onClose");
            }

            @Override
            public void onMessage(String text) {
                Log.e(TAG, "onMessage:" + text);

            }
        });*/
        serialPortText.postDelayed(new Runnable() {
            @Override
            public void run() {
//                WebSocketManager.getInstance().close();
            }
        }, 10000);
        serialPortText.postDelayed(new Runnable() {
            @Override
            public void run() {
//                WebSocketManager.getInstance().connect();
            }
        }, 20000);
    }



    public void onItemClick(View view) {
        int id = view.getId();
        if (id == R.id.testOKhttp) {
            initDiskLogger();

            HttpApi.postJSON("https://www.baidu.com", new Object(), new ResponseString() {
                @Override
                public void onResponseUI(Call call, String processObj) {
                    Logger.e("http-logger-test:" + processObj);
                }
            });
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    String ret = HttpApi.postSyncJSON("https://www.baidu.com", "", String.class);
                    Log.e("TAG", "ret:" + ret);
                }
            }.start();


        } else if (id == R.id.selectFile) {

            // 内含权限请求
            FileSelectorSettings settings = new FileSelectorSettings();
            settings.setRootPath(FileSelectorSettings.getSystemRootPath())//起始路径
                    .setMaxFileSelect(9)//最大文件选择数
                    .setTitle("选择文档")//标题
                    .setFileTypesToShow(".pdf", ".jpg", ".jpeg", ".xls", ".xlsx", ".doc", ".docx", ".txt")
                    .setFileTypesToSelect(FileInfo.FileType.File)//可选择文件类型
                    .show(view.getContext(), new FileSelectorSettings.CallBack() {
                        @Override
                        public void onResult(int resultCode, ArrayList<String> pathList) {
                            Log.e(TAG, "=======" + pathList.toString());

                        }
                    });//显示
        } else if (id == R.id.logview) {
            ToastUtil.showToastShort(this, "如果未弹出日志框，授予该应用 【悬浮窗权限】！！！ 所有应用->找到该应用->权限管理 ");
            LogcatViewer.showLogcatLoggerView(TCMainActivity.this);
            Log.e("tmp", "logview");

            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                ToastUtil.showToastShort(this, "通知权限未打开，注意通知权限下面有通道，也需要打开!");
            } else {
                ToastUtil.showToastShort(this, "如未弹出通知，请手动打开通知下的通道!");
            }
        } else if (id == R.id.notify_test) {

            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                ToastUtil.showToastShort(this, "通知权限未打开，注意通知权限下面有通道，也需要打开!");
            } else {
                ToastUtil.showToastShort(this, "如未弹出通知，请手动打开通知下的通道!");
                // 快捷调起
                /**
                 *   Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                 *     intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                 *     intent.putExtra(Settings.EXTRA_CHANNEL_ID, myNotificationChannel.getId());
                 *     startActivity(intent);
                 */
                Notify.notifcation(this, "title", "message notify");
            }
        } else if (id == R.id.ui_sv_h) {
            startActivity(new Intent(TCMainActivity.this, WidgetStepViewHActivity.class));
        } else if (id == R.id.ui_sv_v) {
            startActivity(new Intent(TCMainActivity.this, WidgetStepViewVActivity.class));
        } else if (id == R.id.ui_livelike) {
            startActivity(new Intent(TCMainActivity.this, TCLiveLikeActivity.class));
        } else if (id == R.id.ui_radarView) {
            startActivity(new Intent(TCMainActivity.this, TCRadarActivity.class));
        } else if (id == R.id.ui_select_pic) {
            startActivity(new Intent(TCMainActivity.this, TCMultiSelectPicActivity.class));
        } else if (id == R.id.ui_ganguView) {
            startActivity(new Intent(TCMainActivity.this, TCGaugeActivity.class));
        }
        // --------- UI widgets -------------------------------<<<<<< end

        else if (id == R.id.qrCode) {
            startActivity(new Intent(TCMainActivity.this, CaptureActivity.class));
        } else if (id == R.id.crcAll) {//底部对话框
            for (CRCModel crcModel : CRCModel.values) {
                String names = Arrays.toString(crcModel.names);
                Log.e(TAG,names + " checkSum: " + new BitwiseBigCRC(crcModel).hex(CRCModel.checkInput));
                if (crcModel.width <= 64) {
                    Log.e(TAG,names + " checkSum: " + new BitwiseCRC(crcModel).hex(CRCModel.checkInput));
                    Log.e(TAG,names + " checkSum: " + new TableDrivenCRC(crcModel).hex(CRCModel.checkInput));
                }
            }
        } else if (id == R.id.bottomDialog) {//底部对话框
            showBottomListDialog();
        } else if (id == R.id.dragExit) {
            startActivity(new Intent(TCMainActivity.this, TCDrag2RightExitActivity.class));
        } else if (id == R.id.fragmentContainer) {
            TCContainerActivity.openSelf(TCMainActivity.this, EmptyFragment.class, "Fragment Container");
        } else if (id == R.id.recyclerView) {
            TCContainerActivity.openSelf(TCMainActivity.this, RVListFragment.class, "RV下拉刷新，上拉更多");
        } else if (id == R.id.permission) {// 请求权限
            requestPermissionMethod();
        } else if (id == R.id.faceManager) {
            ToastUtil.showToastLong(this, "已移除");
        } else if (id == R.id.facePlus) {
            ToastUtil.showToastLong(this, "已移除");
        } else if (id == R.id.facePlusRGB) {
            ToastUtil.showToastLong(this, "已移除");
            btUtil.searchBT();
        } else if (id == R.id.ttsPlay) {
            ToastUtil.showToastLong(this, "已移除");
            new Thread() {
                @Override
                public void run() {
                    Map<String, String> map = new HashMap<>();
                    map.put("aaa", "bbb");
                    map.put("jccc", "ddd");
                    TmpTest ret = HttpApi.postSyncJSON("http://106.52.87.196:8391/api/test", map, TmpTest.class);
                    Log.e("TAG", new Gson().toJson(ret));

                }
            }.start();
        } else if (id == R.id.jsBridge) {
            Bundle bundle = new Bundle();
            bundle.putString("url", "http://www.imiduoduo.com");
            TCContainerActivity.openSelf(TCMainActivity.this, WebViewFragment.class, bundle);
        } else if (id == R.id.btnSerialPort) {
            String port = serialPortText.getText().toString().trim();
            String band = serialPortBand.getText().toString().trim();
            if ("".equals(port) || "".equals(band)) {
                Toast.makeText(this, "串口及波特率不能为空", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, TCSerialPortActivity.class);
            intent.putExtra("port", port);
            intent.putExtra("band", band);
            startActivity(intent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        btUtil.setActivity(this);

    }

    @Override
    protected void onDestroy() {
        btUtil.deleteObserver(onDataReceive);
        super.onDestroy();
    }


    // -------------------------- test method --------------------->>>>>>


    private final Observer onDataReceive = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (arg instanceof BTUtil.BTConnect) {
                runOnUiThread(() -> {
                    BTUtil.BTConnect connect = (BTUtil.BTConnect) arg;
                    Log.e("BT", connect.device.getName() + "\n" + connect.device.getAddress());
                });

                return;
            }
            if (arg instanceof BTUtil.BTDisconnect) {
                Log.e("BT", "BTDisconnect");
                return;
            }
            BTUtil.BTMsg btMsg = (BTUtil.BTMsg) arg;
            Log.e("BT", "recv:" + HexDump.dumpHexString(btMsg.payload));
            if (!btMsg.validPacket) {
                Log.e("BT", "recv data failed!");
                return;
            }

        }
    };

    private void showBottomListDialog() {
        loadingDialog.show();


        BottomListDialog.showSimpleDialog(TCMainActivity.this, new BottomListDialog.OnItemClickListener() {
            @Override
            public void onClick(View view, int pos, BottomSheetDialog dialog) {
                new UpdateUtil(view.getContext(),
                        "http://106.12.167.128:8391/storage/appversion/1/es-mobile-v1.0.apk",
                        "es-mobile-v1.0.apk", "aaaa111", "bbbbb222")
                        .update();
                Toast.makeText(TCMainActivity.this, "You clicked pos is " + pos + " and view id is " + view.getId(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }, new String[]{"Group 1 Item 0", "Group 1 Item 1", "Group 1 Item 2",}, new String[]{"Group 2 Item 0", "Group 2 Item 1", "Group 2 Item 2",});
    }


    private void requestPermissionMethod() {
        // 一次请求单个或多个权限,
        // 1. 申请的权限必须先在manifest中配置， 否则申请结果总是失败
        // 2. ACCESS_FINE_LOCATION 权限包含 ACCESS_COARSE_LOCATION 粗略定位权限
        // 3. 按照权限请求顺序进行申请，遇到失败则返回，之后权限则处于未申请状态: -1
        String permissions[] = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION};
        int requestCode = 7777;
        PermissionHelper.request(TCMainActivity.this, permissions, requestCode, new PermissionHelper.Callback() {

            @Override
            public void onResult(int requestCode, String[] permissions, int[] grantResult) {
                if (PackageManager.PERMISSION_GRANTED == grantResult[0]) {
                    //android.permission.CAMERA 授权成功
                }
                // 授权结果
                Toast.makeText(TCMainActivity.this, "请求权限：" + Arrays.toString(permissions) + "  授权结果:" + Arrays.toString(grantResult), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void testRS485_1_2() {
        /*      testRS485_1();
        testRS485_2();*/
      /*  new Thread(){
            @Override
            public void run() {
                super.run();
                Log.e("TAG", "++++++++++++++++++++++++++++++");
                while (true) {
                    if(f1 & f2) {
                        f1 = false;
                        f2 = false;
                        Log.e("TAG", "===========================");
                        testRS485_1();
                        testRS485_2();
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }.start();*/
    }


    private void initDiskLogger() {
        PermissionHelper.request(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10, new PermissionHelper.Callback() {
            @Override
            public void onResult(int requestCode, String[] permissions, int[] grantResult) {
                // 日志，及cookie使用示例
                CookieJar cookieJar = new CookieJarImpl(new PersistentCookieStore(TCMainActivity.this));
                //CookieJar cookieJar = new CookieJarImpl(new MemoryCookieStore());
                // HttpLogger 日志

                MyAndroidLogAdapter.getInstance().init(TCMainActivity.this.getFilesDir().getAbsolutePath());
                HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
                logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder()
                        .cookieJar(cookieJar)
                        .addInterceptor(new HttpLogInterceptor(true))
                        .addNetworkInterceptor(logInterceptor)
                        .build();
                HttpApi.setClient(client);
                HttpApi.setDebug(true);
                Logger.e("here");
                Logger.i("here");
                Logger.i("here----");
            }
        });

    }

    private boolean f1 = true;
    private boolean f2 = true;

    private void testRS485_1() {
        RS485SerialPortUtilNew rs = new RS485SerialPortUtilNew(10);
        rs.open(115200, "/dev/ttyS3", null, false);
        byte[] sendArray = new String("hellosssss33333world").getBytes();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < 10; i++) {

                    byte[] revArray = rs.sendDataSync(sendArray, 100, 400);
                    Log.e("TMP33", Thread.currentThread().getId() + ":" + ":" + (revArray != null ? new String(revArray) : "ret is null"));
                }
                rs.destroy();
                f1 = true;
            }
        }.start();
    }

    private void testRS485_2() {
        RS485SerialPortUtilNew rs = new RS485SerialPortUtilNew(10);
        rs.open(4800, "/dev/ttyS4", null, false);
        byte[] sendArray = "hhhhhmmmmmmmmmmyyyyy".getBytes(StandardCharsets.UTF_8);
        byte[] sendArray1 = "mmmbbyyy".getBytes(StandardCharsets.UTF_8);
        byte[] sendArrayIII = "ininini".getBytes(StandardCharsets.UTF_8);
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < 10; i++) {
                    rs.sendData(new RS485SerialPortUtilNew.RSCallback(sendArray1, 100, 400) {
                        @Override
                        public void onReceiveFinish(byte[] recBytes) {
                            Log.e("TAG", Thread.currentThread().getId() + ": aa -> " + (recBytes != null ? new String(recBytes) : "ret is null"));
                        }
                    });
                }
                for (int i = 0; i < 5; i++) {
                    rs.sendDataInsertHead(new RS485SerialPortUtilNew.RSCallback(sendArrayIII, 100, 400) {
                        @Override
                        public void onReceiveFinish(byte[] recBytes) {
                            Log.e("TAG", Thread.currentThread().getId() + ": in -> " + (recBytes != null ? new String(recBytes) : "ret is null"));
                        }
                    });
                }

                for (int i = 0; i < 20; i++) {
                    byte[] revArray = rs.sendDataSync(sendArray, 100, 400);
                    Log.e("TAG", Thread.currentThread().getId() + ": tt -> " + (revArray != null ? new String(revArray) : "ret is null"));
                }


                rs.destroy();
                f2 = true;
            }
        }.start();
    }


}
