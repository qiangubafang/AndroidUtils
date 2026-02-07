package org.tcshare.utils.rs485serial;

import android.util.Log;

import androidx.annotation.NonNull;

import org.tcshare.utils.HexDump;
import org.tcshare.utils.ShellUtils;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *
 * 如果要使用232功能， 使用 SerialPort.java 类即可。 获取到输入输出流，然后实现流操作即可。 （硬件自动使能收发） <br/>
 *
 * 程序读发送寄存器，保证发送完成后，再使能接收<br/>
 * 1. 如果内核有gpio的驱动则使用ioctl <br/>
 * 2. 如果内核没有驱动则使用system(echo > 1 /sys/class/gpio/value)来设置
 * 3. 如果带有自动转换的芯片，则使能路径传null （大多数情况下，成品设备都是这种情况）
 *
 *
 */
public class RS485SerialPortUtilNew {
    private static final String TAG = RS485SerialPortUtilNew.class.getSimpleName();
    private boolean DEBUG = false;

    static {
        System.loadLibrary("tc_serialport_n");
    }

    private RS485SerialPort rs485SerialPort  = new RS485SerialPort();
    private WriteReadThread mWriteReadThread;
    private boolean mWriteReadThreadStop = false;
    private boolean isOpenSerialSuccess;
    private LinkedBlockingDeque<RSCallback> rsBlockingQueue; // 优先级队列需80%的CPU；使用双端队列，添加到头部，23%的CPU
    private long waitSlaverTime = 10; // 使用等待从机模式， 从机释放总线可能比较慢, ms
    private boolean syncSending = false;
    private final Object locker = new Object();

    public RS485SerialPortUtilNew() {
        this(Integer.MAX_VALUE, 100);
    }

    public RS485SerialPortUtilNew(int queueSize, long waitSlaverTime) {
        rsBlockingQueue = new LinkedBlockingDeque<>(queueSize);
//        rsBlockingQueue = new PriorityQueue<>(queueSize);
        this.waitSlaverTime = waitSlaverTime;
    }

    public RS485SerialPortUtilNew(long waitSlaverTime) {
        this(1000, waitSlaverTime);
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
            mWriteReadThread = new WriteReadThread();
            mWriteReadThread.start();
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
            if (mWriteReadThread != null) {
                mWriteReadThreadStop = true;
                mWriteReadThread.interrupt();
                mWriteReadThread = null;
            }
            if (rs485SerialPort != null) {
                rs485SerialPort.close();
                rs485SerialPort = null;
            }
            rsBlockingQueue.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cb
     */
    public void sendData(@NonNull final RSCallback cb) {
        try {
            rsBlockingQueue.put(cb);
//            rsBlockingQueue.add(cb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步发送
     * @param sendArray
     * @param revBufSize
     * @param readWaitTime
     */
    public byte[] sendDataSync(byte[] sendArray, int revBufSize, int readWaitTime) {
        byte[] recBytes = null;
        if (DEBUG) Log.d(TAG, "------rsync send Bytes: " + HexDump.toHexString(sendArray));
        synchronized (locker) { // 枷锁目的，同步异步混着用
            syncSending = true;
            recBytes = rs485SerialPort.send(sendArray, revBufSize, readWaitTime);
            syncSending = false;
        }
        if (DEBUG) Log.d(TAG, "------rsync recv Bytes: " + (recBytes == null ? "null " : HexDump.toHexString(recBytes)));
        if (waitSlaverTime > 0) {
            try {
                Thread.sleep(waitSlaverTime);
            } catch (Exception e) {
            }
        }
        return recBytes;

    }

    /**
     * @param cb
     */
    public void sendDataInsertHead(@NonNull final RSCallback cb) {
        cb.level = Long.MAX_VALUE; // 使用优先级队列，设置为最大值

        try {
            rsBlockingQueue.addFirst(cb); // 使用LinkedBlockingDeque队列时，直接添加到队列头部
//            rsBlockingQueue.add(cb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int queueSize() {
        return rsBlockingQueue.size();
    }

    public boolean contains(RSCallback rs){
        return rsBlockingQueue.contains(rs);
    }


    private class WriteReadThread extends Thread {

        public WriteReadThread() {
            android.os.Process.setThreadPriority(-19);//priority：【-20, 19】，高优先级 -> 低优先级
            //setPriority (10); //priority：【1, 10】，低优先级 -> 高优先级。
        }


        @Override
        public void run() {
            super.run();
            while (!mWriteReadThreadStop && !isInterrupted()) {
                try {
                    RSCallback obj;
                    while ((obj = rsBlockingQueue.poll(10, TimeUnit.MILLISECONDS)) == null) {
//                    while ((obj = rsBlockingQueue.poll()) == null) {
                        if(syncSending){
                            try {
                                Thread.sleep(1);
                            }catch (Exception e){}
                        }else if(rs485SerialPort != null){
                            synchronized (locker) {
                                rs485SerialPort.drain(1);
                            }
                        }
                    }

                    if (obj.sendBytes.length == 0) {
                        obj.onReceiveFinish(null);
                    } else {
                        if (DEBUG) Log.d(TAG, "------send Bytes: " + HexDump.toHexString(obj.sendBytes));
                        byte[] recBytes=null;
                        synchronized (locker) {
                            recBytes = rs485SerialPort.send(obj.sendBytes, obj.recBufLen, obj.waitTime);
                        }
                        if (DEBUG) Log.d(TAG, "------recv Bytes: " + (recBytes == null ? "null " : HexDump.toHexString(recBytes)));
                        obj.onReceiveFinish(recBytes);

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
                    RS485SerialPortUtilNew.this.destroy();
                    return;
                }
            }
        }
    }

    public static abstract class RSCallback  implements Comparable<RSCallback>{
        /**
         * 需要发送的数据
         */
        public byte[] sendBytes;
        /**
         * 接收缓冲区的长度
         */
        public int recBufLen;
        /**
         * 读取超时时间 ms
         */
        public int waitTime;
        /**
         * 用于排序的优先级，越大越靠前
         */
        public long level = 0;
        /**
         * 用于判断两个对象是否是相等，如果ID相等，则对象相等
         */
        public String id;

        public RSCallback setId(String id) {
            this.id = id;
            return this;
        }

        public RSCallback setLevel(long level) {
            this.level = level;
            return this;
        }

        public RSCallback(@NonNull byte[] sendData, int recBufLen, int waitTime) {
            this.sendBytes = sendData;
            this.recBufLen = recBufLen;
            this.waitTime = waitTime;
        }

        public RSCallback(@NonNull byte[] sendData, int recBufLen, int waitTime, long level) {
            this.sendBytes = sendData;
            this.recBufLen = recBufLen;
            this.waitTime = waitTime;
            this.level = level;
        }

        /**
         * @param recBytes may be null
         */
        public abstract void onReceiveFinish(byte[] recBytes);


        @Override
        public int compareTo(RSCallback o) {
            return Long.compare(o.level, level);
        }

        @Override
        public boolean equals(Object o) {
            RSCallback that = (RSCallback) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class EmptyRSCallback extends RSCallback{

        public EmptyRSCallback(String id) {
            super(new byte[0], 0, 0);
            super.id = id;
        }

        @Override
        public void onReceiveFinish(byte[] recBytes) {

        }
        @Override
        public boolean equals(Object o) {
            RSCallback that = (RSCallback) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
