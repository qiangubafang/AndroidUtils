package org.tcshare.app.amodule.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.tcshare.app.R;
import org.tcshare.app.amodule.dialog.UsbSelectDialog;
import org.tcshare.utils.hex.DecoderException;
import org.tcshare.utils.hex.Hex;
import org.tcshare.utils.hex.HexDump;
import org.tcshare.utils.usbserial.driver.UsbSerialDriver;
import org.tcshare.utils.usbserial.driver.UsbSerialPort;
import org.tcshare.utils.usbserial.driver.UsbSerialProber;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class TCUSBSerialPortActivity extends Activity {

    private static final String TAG = TCUSBSerialPortActivity.class.getSimpleName();

    private int mSendTimeInterval;
    private EditText mTimeInterval;
    private Button sendBtn;
    private TextView sendCountTV;
    private TextView recCountTV;
    private long sendCount;
    private long receiveCount;
    private EditText sendText;
    private TextView mReception;
    private CheckBox repeatCheckBox;
    private CheckBox hexCheckBox;
    private OnDataReceive receiveCallBack = new OnDataReceive() {
        @Override
        public void onReceived(final byte[] buffer, final int size) {
            runOnUiThread(new Runnable() {
                public void run() {
                    byte[] result = new byte[size];
                    System.arraycopy(buffer, 0, result, 0, size);

                    String data = hexCheckBox.isChecked() ? data = Hex.encodeHexString(result) : new String(result);

                    mReception.append("recv->" + data + "\n");
                    receiveCount += size;
                    recCountTV.setText(String.valueOf(receiveCount));

                    scrollToBottom(mReception);
                }
            });
        }
    };
    private UsbSelectDialog usbSelectDialog;


    private void DisplayError(String msg) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(msg);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TCUSBSerialPortActivity.this.finish();
            }
        });
        b.show();
    }

    // 让 TextView 自动滚动到底部（真正可用）
    private void scrollToBottom(TextView textView) {
        textView.post(new Runnable() {
            @Override
            public void run() {
                // 核心：计算文本总高度，滚动到最底部
                int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
                if (scrollAmount > 0) {
                    textView.scrollTo(0, scrollAmount);
                } else {
                    textView.scrollTo(0, 0);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc_usb_serialport);

        sendText = (EditText) findViewById(R.id.sendContent);
        mReception = (TextView) findViewById(R.id.recevieContent);
        sendCountTV = (TextView) findViewById(R.id.send_num);
        recCountTV = (TextView) findViewById(R.id.rec_num);

        findViewById(R.id.statResetBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiveCount = 0;
                sendCount = 0;
                recCountTV.setText("");
                sendCountTV.setText("");
            }
        });
        findViewById(R.id.clearbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiveCount = 0;
                sendCount = 0;
                recCountTV.setText("");
                sendCountTV.setText("");
                sendText.setText("");
                mReception.setText("");
            }
        });
        mReception.setMovementMethod(new ScrollingMovementMethod());

        mSendTimeInterval = 1000;
        mTimeInterval = (EditText) findViewById(R.id.timeinterval);

        hexCheckBox = (CheckBox) findViewById(R.id.cb_hex);

        repeatCheckBox = (CheckBox) findViewById(R.id.repeat_check);
        repeatCheckBox.setChecked(false);
        repeatCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            private Timer timer;

            @Override
            public void onCheckedChanged(CompoundButton arg0,
                                         boolean isChecked) {
                if (isChecked) {
                    if (!TextUtils.isEmpty(mTimeInterval.getText())) {
                        mSendTimeInterval = Integer.parseInt(mTimeInterval.getText().toString());
                    }

                    final byte[] sendbytes = getSendData();
                    if (sendbytes != null) {
                        timer = new Timer(true);
                        TimerTask task = new TimerTask() {
                            public void run() {
                                sendData(sendbytes);
                            }
                        };
                        timer.schedule(task, mSendTimeInterval, mSendTimeInterval);
                        sendText.setEnabled(false);
                        sendBtn.setEnabled(false);
                    } else {
                        Toast.makeText(TCUSBSerialPortActivity.this, "请输入 0~9,a~f,A~F,内的值，不含空格.", Toast.LENGTH_SHORT).show();
                        repeatCheckBox.setChecked(false);
                    }

                    if (mSendTimeInterval <= 0) {
                        Toast.makeText(getBaseContext(), "时间间隔必须大于 0 ms!", Toast.LENGTH_SHORT).show();
                        repeatCheckBox.setChecked(false);
                    } else {
                        mTimeInterval.setEnabled(false);
                    }
                } else {
                    sendBtn.setEnabled(true);
                    sendText.setEnabled(true);
                    mTimeInterval.setEnabled(true);
                    if (timer != null) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            }
        });

        sendBtn = (Button) findViewById(R.id.sendbtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!sendData(getSendData())) {
                    Toast.makeText(TCUSBSerialPortActivity.this, "请输入 0~9,a~f,A~F,内的值，不含空格.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        registerUSBReceiver();
        showUSBSelectDialog();
    }



    public final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            Logger.d(TAG, "onReceive: " + intent.toString());
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    requestUSBPermission(device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                requestUSBPermission(device);
                Toast.makeText(TCUSBSerialPortActivity.this, "插入新设备！", Toast.LENGTH_SHORT).show();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Toast.makeText(TCUSBSerialPortActivity.this, "设备已拔出！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void open(UsbManager usbManager, UsbDevice usbDevice) {
        try {
            UsbDeviceConnection connection = usbManager.openDevice(usbDevice);

            UsbSerialDriver usbSerialDriver = UsbSerialProber.getDefaultProber().probeDevice(usbDevice);
            UsbSerialPort port = usbSerialDriver.getPorts().get(0);
            port.open(connection);
            port.setDTR(true);
            port.setRTS(true);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            if (port.isOpen()) {
                start(port, receiveCallBack);
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Thread reader;
    private Thread writer;
    private boolean running;


    private static final int WRITE_WAIT_MILLIS = 500; // 放大
    private static final int READ_WAIT_MILLIS = 500;

    public interface OnDataReceive {
        void onReceived(byte[] buffer, int size);
    }

    public void start(UsbSerialPort port, OnDataReceive callBack) {
        running = true;
        reader = new Thread() {
            private final byte[] buffer = new byte[1024];

            @Override
            public void run() {
                super.run();
                while (running) {
                    try {
                        int numRead = port.read(buffer, READ_WAIT_MILLIS);
                        if (numRead > 0) {
                            byte[] readBytes = new byte[numRead];
                            System.arraycopy(buffer, 0, readBytes, 0, numRead);

                            if (callBack != null) {
                                callBack.onReceived(buffer, numRead);
                            }

                            Logger.v(TAG, "recv: " + HexDump.toHexString(readBytes));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        writer = new Thread() {
            @Override
            public void run() {
                super.run();
                while (running) {
                    try {
                        byte[] data = queue.take();
                        port.write(data, WRITE_WAIT_MILLIS);
                        Logger.d(TAG, "send: " + HexDump.toHexString(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        reader.start();
        writer.start();
    }

    private void registerUSBReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(usbReceiver, filter);
        }

    }

    private static final String ACTION_USB_PERMISSION = "com.rangotec.qgbftoy.USB_PERMISSION";

    private void requestUSBPermission(UsbDevice device) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager.hasPermission(device)) {
            Logger.e(TAG, "已有权限");
            open(usbManager, device);
        } else {
            Intent intent = new Intent(ACTION_USB_PERMISSION);
            intent.putExtra(UsbManager.EXTRA_DEVICE, device);
            intent.setPackage(getPackageName());
            int flag = PendingIntent.FLAG_IMMUTABLE;

            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 9999, intent, flag);
            usbManager.requestPermission(device, permissionIntent);
        }

    }


    private byte[] getSendData() {
        String sendStr = sendText.getText().toString().trim();
        if (hexCheckBox.isChecked()) {
            try {
                return "".equals(sendStr) ? null : Hex.decodeHex(sendStr);
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }
        return "".equals(sendStr) ? null : sendStr.getBytes();
    }

    private final LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
    private void showUSBSelectDialog() {
        if (usbSelectDialog == null) {
            usbSelectDialog = new UsbSelectDialog(this, new UsbSelectDialog.OnSelectListener() {
                @Override
                public void onSelect(UsbSelectDialog dialog, UsbDevice usbDevice) {
                    requestUSBPermission(usbDevice);
                    dialog.dismiss();
                }
            });
        }
        if (!usbSelectDialog.isShowing()) {
            usbSelectDialog.show();
        }
    }
    private boolean sendData(byte[] sendBytes) {
        if (sendBytes != null) {
            try {
                queue.put(sendBytes);
                sendCount += sendBytes.length;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendCountTV.setText(String.valueOf(sendCount));
                    }
                });
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return false;

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbReceiver);
        if (reader != null) {
            reader.interrupt();
        }
        if (writer != null) {
            writer.interrupt();
        }
        super.onDestroy();
    }
}
