package org.tcshare.utils.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.tcshare.androidutils.R;
import org.tcshare.dialog.LoadingDialog;
import org.tcshare.dialog.MsgDialogUtil;
import org.tcshare.utils.ImmerseUtil;
import org.tcshare.utils.ToastUtil;

@SuppressLint("MissingPermission")
public class BTSearchUtil {
    private static final String TAG = BTSearchUtil.class.getSimpleName();
    private Dialog loadingDialog;
    private BluetoothAdapter bluetoothAdapter;
    private Activity ctx;
    private DevFoundListener listener;

    // 检测设备
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "发现设备:" + device.getName() + ":" + device.getAddress() + ":" + (device.getBondState() == BluetoothDevice.BOND_NONE ? "未配对" : "其他"));
                if (rule.validDev(device)) {
                    listener.onFoundDev(device);
                    stopFound();
                    loadingDialog.dismiss();
                }
            }
        }
    };
    private BTUtil.RuleCheck rule;


    public BTSearchUtil(@NonNull Activity context, BluetoothAdapter bluetoothAdapter, DevFoundListener listener) {
        this.ctx = context;
        this.bluetoothAdapter = bluetoothAdapter;
        this.listener = listener;
        mCreateLoadingDialog();
    }

    private void mCreateLoadingDialog() {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_loading_with_cancel, null);
        LinearLayout layout = view.findViewById(R.id.dialog_view);
        ImageView img = view.findViewById(R.id.img);
        TextView tipText = view.findViewById(R.id.tipTextView);

        tipText.setText("正在搜索，请稍后....");

        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.dialog_loading);

        loadingDialog = new Dialog(ctx, R.style.loading_dialog);
        loadingDialog.setOnShowListener(dialog -> {
            img.startAnimation(animation);
        });
        loadingDialog.setOnDismissListener(dialog -> {
            Animation anim = img.getAnimation();
            if(anim != null) {
                anim.cancel();
            }
        });
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            stopFound();
            Toast.makeText(ctx, "停止搜索蓝牙...", Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
        });
    }

    public void stopFound() {
        try {
            bluetoothAdapter.cancelDiscovery();
            ctx.unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
            // 会多次调用停止
        }
    }

    public void starFound(BTUtil.RuleCheck rule) {
        this.rule = rule;

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ctx.registerReceiver(receiver, filter);

        bluetoothAdapter.cancelDiscovery();
        boolean ret = bluetoothAdapter.startDiscovery();
        if (!ret) {
            MsgDialogUtil.showMsg(ctx, "启动搜索蓝牙失败，请检查【蓝牙及GPS】是否已经开启，并且授予了该APP使用蓝牙及GPS的权限！");
        } else {
            Toast.makeText(ctx, "正在搜索蓝牙...", Toast.LENGTH_SHORT).show();
            loadingDialog.show();
        }
    }


    public interface DevFoundListener {
        void onFoundDev(BluetoothDevice device);
    }
}
