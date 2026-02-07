package org.tcshare.utils.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.tcshare.dialog.LoadingDialog;
import org.tcshare.dialog.MsgDialogUtil;
import org.tcshare.utils.HexDump;
import org.tcshare.utils.packet.PacketUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

/**
 * 调用之前，请确保已经有权限， 这里不再检查。
 */
@SuppressLint("MissingPermission")
public class BTUtil extends Observable {
    private static final String TAG = BTUtil.class.getSimpleName();
    private long maxWaitTime = 10_000;
    private Activity act;
    private BTSearchDialog btSearchDialog;
    private BTSearchUtil btSearchUtil;
    private int supportedMTU = 20;
    private BluetoothGatt btGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private final String servicesUUID;   //服务的UUID
    private final String notifyUUID;     // 读
    private final String writeUUID;      // 写

    private byte[] sendBytes;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private Dialog loadingDialog;

    private final Runnable dismissLoadingDialogDelay = () -> reset(maxWaitTime / 1000 + "秒内，未收到蓝牙返回的信息，请重新连接！");
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice connectDev;
    private final IPacketUtil packetUtil;
    /**
     * 如果需要拆分数据包， 两个包之间等待的时间间隔， 小于等于0 则不等待。
     */
    private long packetSegSendDuration = 30;
    private boolean DEBUG = false;

    private int reqMTU = 512; // 请求MTU,

    /**
     *
     * @param servicesUUID 如果为空或null， 则按照notity 和 write uuid来插针。
     * @param notifyUUID
     * @param writeUUID
     * @param maxWaitTime
     * @param packetUtil
     */
    public BTUtil(String servicesUUID, String notifyUUID, String writeUUID, long maxWaitTime, IPacketUtil packetUtil) {
        this.servicesUUID = servicesUUID;
        this.notifyUUID = notifyUUID;
        this.writeUUID = writeUUID;
        this.maxWaitTime = maxWaitTime;
        this.packetUtil = packetUtil;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void setActivity(Activity act) {
        this.act = act;
        loadingDialog = LoadingDialog.createLoadingDialog(act, "正在处理，请稍后...");
        btSearchDialog = new BTSearchDialog(act, bluetoothAdapter, this::connectBT);
        btSearchUtil = new BTSearchUtil(act, bluetoothAdapter, this::connectBT);
    }


    public void reset(String msg) {
        setChanged();
        notifyObservers(new BTDisconnect());

        connectDev = null;
        handler.removeCallbacks(dismissLoadingDialogDelay);
        if(loadingDialog != null) {
            loadingDialog.dismiss();
        }
        if (btGatt != null) {
            btGatt.close();
        }

        packetUtil.resetPacket();
        if (msg != null) {
            Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public int getReqMTU() {
        return reqMTU;
    }

    public void setReqMTU(int reqMTU) {
        this.reqMTU = reqMTU;
    }

    public long getPacketSegSendDuration() {
        return packetSegSendDuration;
    }

    public void setPacketSegSendDuration(long packetSegSendDuration) {
        this.packetSegSendDuration = packetSegSendDuration;
    }

    public void searchBT() {
        btSearchDialog.show();
    }
    public void connectBT(RuleCheck rule) {
        btSearchUtil.starFound(rule);
    }

    protected void connectBT(BluetoothDevice device) {
        this.connectDev = device;
        loadingDialog.show();
        btGatt = device.connectGatt(act, false, new BluetoothGattCallback() {
            private final IPacketUtil.IPacketReadyCallBack cb = new IPacketUtil.IPacketReadyCallBack() {
                @Override
                public void onPacketReady(byte[] recvBytes) {
                    handler.removeCallbacks(dismissLoadingDialogDelay);
                    loadingDialog.dismiss();

                    if(DEBUG) Log.e(TAG, "recevPacket:" + HexDump.toHexString(recvBytes));
                    ValidPacket ret = packetUtil.validPayload(recvBytes);

                    act.runOnUiThread(() -> {
                        setChanged();
                        notifyObservers(new BTMsg(ret.valid, ret.type, ret.payload, sendBytes, recvBytes));
                    });

                }
            };

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                /*    act.runOnUiThread(() -> {
                        Toast.makeText(act, "收到MTU变动:" + mtu, Toast.LENGTH_SHORT).show();
                    });*/
                    supportedMTU = mtu - 3;
                    Log.e(TAG, "onMtuChanged:" + mtu + "support MTU " + (supportedMTU)+ ":" + status);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                String msg = characteristic.getValue() == null ? null : HexDump.toHexString(characteristic.getValue());
                Log.e(TAG, "onCharacteristicRead:" + msg);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                String msg = characteristic.getValue() == null ? null : HexDump.toHexString(characteristic.getValue());
                if(DEBUG) Log.e(TAG, "onCharacteristicWrite:" + msg);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if(DEBUG) Log.e(TAG, "onConnectionStateChange:" + status + "-" + newState);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    act.runOnUiThread(() -> reset("连接失败！" + status));
                    return;
                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    btGatt.discoverServices();
                    if(DEBUG) Log.e(TAG, "GATT连接成功！下一步找对应的服务。");
                } else if(newState ==  BluetoothProfile.STATE_DISCONNECTED){
                    act.runOnUiThread(() -> reset("连接断开！" + status));
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if(DEBUG) Log.e(TAG, "onServicesDiscovered");
                try {
                    List<BluetoothGattService> supportedGattServices;
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // 打印一下所有的服务
                        supportedGattServices = gatt.getServices();
                        for (int i = 0; i < supportedGattServices.size(); i++) {
                            if(DEBUG)  Log.e("success", "1:BluetoothGattService UUID=:" + supportedGattServices.get(i).getUuid());
                            List<BluetoothGattCharacteristic> listGattCharacteristic = supportedGattServices.get(i).getCharacteristics();
                            for (int j = 0; j < listGattCharacteristic.size(); j++) {
                                int charaProp = listGattCharacteristic.get(j).getProperties();
                                String p = ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) ? "属性可读" : "属性不可读";
                                if(DEBUG) Log.e("success", "2:   BluetoothGattCharacteristic UUID=:" + listGattCharacteristic.get(j).getUuid()
                                        + "=" + p
                                        + "=" + listGattCharacteristic.get(j).getWriteType());
                            }
                        }
                    } else {
                        act.runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            MsgDialogUtil.showMsg(act, "找不到服务，无法连接到设备！");
                        });
                        return;
                    }

                    if(servicesUUID != null && !servicesUUID.isEmpty()) {
                        //设置serviceUUID
                        BluetoothGattService bluetoothGattService = btGatt.getService(UUID.fromString(servicesUUID));
                        //设置写入特征UUID
                        writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(writeUUID));
                        //设置监听特征UUID
                        notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(notifyUUID));
                    }else{
                        // 没传serviceUUID，则根据notify和write 的UUID查找
                        for(BluetoothGattService bg : supportedGattServices){
                            List<BluetoothGattCharacteristic> listGattCharacteristic = bg.getCharacteristics();
                            for (BluetoothGattCharacteristic characteristic : listGattCharacteristic) {
                                //notify
                                if (characteristic.getUuid().toString().equals(notifyUUID)) {
                                    notifyCharacteristic = characteristic;
                                }
                                //write
                                if (characteristic.getUuid().toString().equals(writeUUID)) {
                                    writeCharacteristic = characteristic;
                                }
                            }
                        }
                    }


                    //开启监听
                    boolean ret = gatt.setCharacteristicNotification(notifyCharacteristic, true);
                    if (!ret) {
                        act.runOnUiThread(() -> MsgDialogUtil.showMsg(act, "监听蓝牙通知失败！"));
                        return;
                    }


                    List<BluetoothGattDescriptor> descriptors = notifyCharacteristic.getDescriptors();
                    for (BluetoothGattDescriptor descriptor : descriptors) {
                        boolean b1 = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); // 所有的都设置一下允许通知，否则收不到信息
                        if (b1) {
                            gatt.writeDescriptor(descriptor);
                            if(DEBUG) Log.e(TAG, "描述 UUID :" + descriptor.getUuid().toString() + "设置允许通知成功！");
                        } else {
                            if(DEBUG) Log.e(TAG, "描述 UUID :" + descriptor.getUuid().toString() + " 设置允许通知失败");
                        }
                    }

                    if(DEBUG) Log.e("TAG", "设置监听成功!");

                    //gatt.readCharacteristic(notifyCharacteristic); // 读

                    // 个别厂家设备不支持直接配置。
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(300);
                            } catch (Exception e) {
                            }
                            boolean reqMtu = gatt.requestMtu(reqMTU);
                            if(DEBUG) Log.e(TAG, "request " + reqMTU + " mtu:" + reqMtu);

                            act.runOnUiThread(() -> {
                                setChanged();
                                notifyObservers(new BTConnect(device));

                                Toast.makeText(act, "设备连接成功！", Toast.LENGTH_SHORT).show();
                                bluetoothAdapter.cancelDiscovery();
                                btSearchDialog.dismiss();
                            });
                        }
                    }.start();


                } catch (Exception e) {
                    e.printStackTrace();
                    act.runOnUiThread(() -> MsgDialogUtil.showMsg(act, "连接失败，该程序仅支持连接指定设备！"));
                } finally {
                    loadingDialog.dismiss();
                }
            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                if(DEBUG) {
                    Log.e(TAG, "onCharacteristicChanged:" + (characteristic.getValue() == null ? "null" :HexDump.toHexString(characteristic.getValue())));
                }
                packetUtil.preparePacket(characteristic.getValue(), cb);
            }

        });

    }

    public boolean isDEBUG() {
        return DEBUG;
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    public void sendData(byte[] payload) {
        sendData(payload, true);
    }

    public boolean isConnected(){
        return connectDev != null && writeCharacteristic != null && notifyCharacteristic != null;
    }
    public void sendData(byte[] payload, boolean showProgress) {
        if (connectDev == null || writeCharacteristic == null || notifyCharacteristic == null) {
            Toast.makeText(act, "未连接到设备！", Toast.LENGTH_SHORT).show();
            return;
        } else if (payload == null || payload.length == 0) {
            Toast.makeText(act, "发送内容为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (showProgress) {
            loadingDialog.show();
            handler.postDelayed(dismissLoadingDialogDelay, maxWaitTime);// N秒内，未收到蓝牙返回的通知，结束进度框。
        }
        sendBytes = payload;
        if(DEBUG) Log.e(TAG, "需要发送的数据：" + HexDump.toHexString(payload));
        ByteBuffer tmpBB = ByteBuffer.allocate(supportedMTU);
        for (byte b : payload) {
            tmpBB.put(b);
            if (!tmpBB.hasRemaining()) {
                byte[] packet = tmpBB.array();
                if(DEBUG) Log.e(TAG, "sendPacket:" + HexDump.toHexString(packet));
                if (sendPacketFailed(packet)) {
                    tmpBB.clear();
                    if (showProgress) {
                        MsgDialogUtil.showMsg(act, "拆分数据包发送失败，请重试！");
                    }
                    return;
                }
                tmpBB.clear();
            }
        }
        tmpBB.flip();
        byte[] packet = new byte[tmpBB.limit()];
        tmpBB.get(packet);
        if (packet.length > 0) {
            if(DEBUG) Log.e(TAG, "sendPacketFlush:" + HexDump.toHexString(packet));
            if (sendPacketFailed(packet)) {
                if (showProgress) {
                    MsgDialogUtil.showMsg(act, "拆分数据包发送失败，请重试！");
                }
            }
        }

    }


    private boolean sendPacketFailed(byte[] bytes) {
        writeCharacteristic.setValue(bytes);
//        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        boolean ret = btGatt.writeCharacteristic(writeCharacteristic);
        //TODO 使用队列或timer等方式来处理
        if(packetSegSendDuration > 0) {
            try {
                Thread.sleep(packetSegSendDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return !ret;

    }

    public BluetoothDevice getConnectDev() {
        return connectDev;
    }

    public static class BTMsg {
        public final boolean validPacket;
        public final byte[] sendBytes;
        public final byte[] recvBytes;
        public final byte[] payload;
        public final byte type;

        public BTMsg(boolean validPacket, byte type, byte[] payload, byte[] sendBytes, byte[] recvBytes) {
            this.validPacket = validPacket;
            this.type = type;
            this.sendBytes = sendBytes;
            this.recvBytes = recvBytes;
            this.payload = payload;
        }


    }

    public static class BTDisconnect {
    }

    public static class BTConnect {
        public BluetoothDevice device;

        public BTConnect(BluetoothDevice device) {
            this.device = device;
        }
    }

    public interface RuleCheck{
        boolean validDev(BluetoothDevice btDev);
    }
}
