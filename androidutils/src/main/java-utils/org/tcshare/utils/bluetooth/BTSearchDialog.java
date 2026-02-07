package org.tcshare.utils.bluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.tcshare.adapter.DevAdapter;
import org.tcshare.androidutils.R;
import org.tcshare.dialog.MsgDialogUtil;
import org.tcshare.utils.DensityUtil;
import org.tcshare.utils.ToastUtil;
import org.tcshare.widgets.ItemDecorations;

public class BTSearchDialog extends Dialog {
    private static final String TAG = BTSearchDialog.class.getSimpleName();
    private final OnDevSelectListener listener;
    private final BluetoothAdapter bluetoothAdapter;
    private final Activity ctx;
    private final DevAdapter adapter = new DevAdapter();


    // 检测设备
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, "发现设备:" + device.getName() + ":" + device.getAddress() + ":" + (device.getBondState() == BluetoothDevice.BOND_NONE ? "未配对" : "其他"));
                adapter.addItem(device);
            }
        }
    };

    public BTSearchDialog(@NonNull Activity context, BluetoothAdapter bluetoothAdapter, OnDevSelectListener listener) {
        super(context);
        this.ctx = context;
        this.bluetoothAdapter = bluetoothAdapter;
        this.listener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bt_search_layout);


        RecyclerView mRecyclerView = findViewById(R.id.rvDevList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        mRecyclerView.addItemDecoration(ItemDecorations.vertical(ctx).typeColor(0, Color.parseColor("#efefef"), DensityUtil.dp2px(ctx, 1)).create());
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickLitener((view, item) -> listener.onConnect(item));


        findViewById(R.id.btnClose).setOnClickListener(v -> {
            bluetoothAdapter.cancelDiscovery();
            dismiss();
        });
        findViewById(R.id.btnSearch).setOnClickListener(v -> startSearchDialog());
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ctx.registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        ctx.unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void show() {
        adapter.clear();
        super.show();
    }

    private void startSearchDialog() {
        adapter.clear();
        bluetoothAdapter.cancelDiscovery();
        boolean ret = bluetoothAdapter.startDiscovery();
        if (!ret) {
            MsgDialogUtil.showMsg(ctx, "启动搜索蓝牙失败，请检查【蓝牙及GPS】是否已经开启，并且授予了该APP使用蓝牙及GPS的权限！");
        } else {
            Toast.makeText(ctx, "正在搜索蓝牙...", Toast.LENGTH_SHORT).show();
        }
    }


    public interface OnDevSelectListener {
        void onConnect(BluetoothDevice device);
    }
}
