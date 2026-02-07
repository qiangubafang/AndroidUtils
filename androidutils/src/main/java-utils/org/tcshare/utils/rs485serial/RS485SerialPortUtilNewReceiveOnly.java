package org.tcshare.utils.rs485serial;

import android.util.Log;

import androidx.annotation.NonNull;

import org.tcshare.utils.ShellUtils;

/**
 * 该功能与 RS485SerialPortUtilNew.java 的区别是，该类只读。
 *
 * 功能描述见 RS485SerialPortUtilNew.java
 *
 */
public class RS485SerialPortUtilNewReceiveOnly {
    private static final String TAG = RS485SerialPortUtilNewReceiveOnly.class.getSimpleName();
    private boolean DEBUG = false;

    static {
        System.loadLibrary("tc_serialport_n");
    }

    private RS485SerialPort rs485SerialPort  = new RS485SerialPort();
    private ReadOnlyThread mReadOnlyThread;
    private boolean mWriteReadThreadStop = false;
    private boolean isOpenSerialSuccess;
    private long waitSlaverTime = 10; // 使用等待从机模式， 从机释放总线可能比较慢, ms
    private final Object locker = new Object();

    public RS485SerialPortUtilNewReceiveOnly() {
        this(100);
    }

    public RS485SerialPortUtilNewReceiveOnly(long waitSlaverTime) {
        this.waitSlaverTime = waitSlaverTime;
    }


    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    /**
     * 使用示例：
     *    普通串口                     open(9600, "/dev/ttyS1", null, false);                        // 有单独芯片控制自动使能收发；
     *    修改过硬件的设备              open(9600, "/dev/ttyS1", “/sys/class/gpioXXX/value" , false);  // 可以控制gpio， 但无法改内核（如没有源码）
     *    修改过硬件并可以修改内核的设备  open(9600, "/dev/ttyS1", “/dev/gpioXXXX" , true);              // 内核控制使能收发
     *
     * @param band      波特率
     * @param port      串口路径
     * @param rs485Path 使能节点，示例：无内核驱动， echo (1|0) > /sys/class/gpioXXX/value 有内核驱动 /dev/gpioXXX;
     *                  如果芯片带有自动转换，则传入null（当作普通串口）
     * @param hasDriver 是否同时修改了内核驱动，无 false;
     *                  如果有内核驱动， 请先注销掉对应接口！， 否则会导致打不开； 注销示例：  echo 68 > /sys/class/gpio/unexport
     */
    public void open(int band, @NonNull String port, String rs485Path, boolean hasDriver) {
        try {
            // 注意要让APP拥有ROOT 权限
            String cmdPort = "chmod 777 " + port;
            ShellUtils.CommandResult cmdPortResult = ShellUtils.execCommand(cmdPort, true);
            Log.d(TAG, String.format("exe command: %s result %s", cmdPort, cmdPortResult.toString()));

            if(rs485Path != null) {
                String cmdEnable = "chmod 777 " + rs485Path;
                ShellUtils.CommandResult cmdEnableResult = ShellUtils.execCommand(cmdEnable, true);
                Log.d(TAG, String.format("exe command: %s result %s", cmdEnable, cmdEnableResult.toString()));
            }

            int result = rs485SerialPort.open(port, rs485Path, band, 0, hasDriver);
            mWriteReadThreadStop = false;
            mReadOnlyThread = new ReadOnlyThread();
            mReadOnlyThread.start();
            if(result != -1) {
                isOpenSerialSuccess = true;
            }else{
                isOpenSerialSuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "打开串口时，异常");
            isOpenSerialSuccess = false;
        } finally {
        }
    }

    public boolean isOpenSerialSuccess() {
        return isOpenSerialSuccess;
    }


    public void destroy() {
        try {
            if (mReadOnlyThread != null) {
                mWriteReadThreadStop = true;
                mReadOnlyThread.interrupt();
                mReadOnlyThread = null;
            }
            if (rs485SerialPort != null) {
                rs485SerialPort.close();
                rs485SerialPort = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ReadOnlyThread extends Thread {

        public ReadOnlyThread() {
            android.os.Process.setThreadPriority(-19);//priority：【-20, 19】，高优先级 -> 低优先级
            //setPriority (10); //priority：【1, 10】，低优先级 -> 高优先级。
        }


        @Override
        public void run() {
            super.run();
            while (!mWriteReadThreadStop && !isInterrupted()) {
                try {
                    byte[] ret = null;
                    while ( (ret = rs485SerialPort.drain(300)) != null){
                        if(onReceiveCallback != null && ret.length > 0){
                            onReceiveCallback.onReceive(ret);
                        }

                        // 如果设置了等待，则等一会单片机
                        if (waitSlaverTime > 0) {
                            try {
                                Thread.sleep(waitSlaverTime);
                            } catch (Exception e) {
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    isOpenSerialSuccess = false;
                    RS485SerialPortUtilNewReceiveOnly.this.destroy();
                    return;
                }
            }
        }
    }
    private OnReceiveCallback onReceiveCallback;

    public void setOnReceiveCallback(OnReceiveCallback onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
    }

    public interface OnReceiveCallback{
        void onReceive(byte[] data);
    }
}
