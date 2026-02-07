package org.tcshare.utils.rs485serial;

/**
 * 485串口通信
 */
public class RS485SerialPort {


    private int id = -1;

    /**
     * 注意使用system命令会有延时，需要设置从设备的延时时间， （CPU越快计时越精确）
     *
     * @param devPath 串口设备树
     * @param enableIO 使能节点，示例：无内核驱动， echo (1|0) > /sys/class/gpioXXX/value 有内核驱动 /dev/gpioXXX;
     *                 如果芯片带有自动转换，则传入null（当作普通串口）
     * @param baudRate 波特率
     * @param flags 打开标识
     * @param hasDriver 是否同时修改了内核驱动，无 false，
     * @return
     */
    public native int nativeOpen(String devPath, String enableIO, int baudRate, int flags, boolean hasDriver);

    /**
     * 1ms超时，拉取字节
     */
    public native byte[] nativeDrain(int id, int time);

    /**
     * 向串口发送数据
     * @param sendArray 发送内容
     * @param revBufSize 接收缓冲区
     * @param readWaitTime 读等待超时时间 ms
     * @return 接收到的字节 或 null
     */
    public native byte[] nativeSend(int id, byte[] sendArray, int revBufSize, int readWaitTime);

    /**
     * 关闭
     */
    public native void nativeClose(int id);



    // java ---------------------------------------------------------------------
    // Java 调用方法， 这里，每个java对象单独分配一个c++对象，来处理底层逻辑
    public int open(String devPath, String enableIO, int baudRate, int flags, boolean hasDriver){
        id = nativeOpen(devPath, enableIO, baudRate, flags, hasDriver);
        return id;
    }

    public void close(){
        if(id <= 0){
            return;
        }
        nativeClose(id);
    }

    public byte[] drain(int time){
        if(id <= 0){
            return new byte[0];
        }
        return nativeDrain(id, time);
    }

    public byte[] send(byte[] sendArray, int revBufSize, int readWaitTime){
        if(id <= 0){
            return new byte[0];
        }
        return nativeSend(id, sendArray, revBufSize, readWaitTime);
    }
}
