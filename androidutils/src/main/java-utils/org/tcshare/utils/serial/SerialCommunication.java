package org.tcshare.utils.serial;

import org.tcshare.utils.packet.IPacket;
import org.tcshare.utils.packet.IPacketCallback;
import org.tcshare.utils.packet.PacketSimpleUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 异步全双工串口通信功能 <br/>
 * 抽取到代码库 <br/>
 * 数据包格式 <br/>
 *      包头	  长度	  内容	     校验	包尾 <br/>
 *      1	  2	      1-N个字节	 2	    1 <br/>
 */
public class SerialCommunication {
    private static final String TAG = SerialCommunication.class.getSimpleName();
    private final String port;
    private final int band;
    private final IPacketCallback cb;
    private final IPacket psu;
    private SerialPort mSerialPort;
    private ReadThread mReadThread;
    private WriteThread mWriteThread;
    private boolean connected = false; // 是否链接上了


    public SerialCommunication(String port, int band, IPacket psu, IPacketCallback cb) {
        this.port = port;
        this.band = band;
        this.cb = cb;
        this.psu = psu;
    }

    /**
     * 仅传递负载，psu 构造包
     * @param payload
     * @return
     */
    public boolean sendPacket(byte[] payload) {
        if (mWriteThread != null && connected) {
            mWriteThread.sendData(psu.gen(payload));
            return true;
        }else{
            return false;
        }
    }

    /**
     * 自定义包
     * @param packet
     * @return
     */
    public boolean sendPacketFull(byte[] packet) {
        if (mWriteThread != null && connected) {
            mWriteThread.sendData(packet);
            return true;
        }else{
            return false;
        }
    }
    public boolean isConnected(){
        return connected;
    }

    public boolean connect() {
        try {
            disconnect();
            mSerialPort = new SerialPort(port, band, 0);

            mReadThread = new ReadThread(mSerialPort.getInputStream());
            mReadThread.start();
            mWriteThread = new WriteThread(mSerialPort.getOutputStream());
            mWriteThread.start();

            connected = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
            return false;
        }
    }

    public void disconnect() {
        try {
            mReadThread.interrupt();
            mSerialPort.getInputStream().close();
        } catch (Exception e) {
        }
        mReadThread = null;
        try {
            mWriteThread.interrupt();
            mSerialPort.getOutputStream().close();
        } catch (Exception e) {
        }
        mWriteThread = null;
        try {
            mSerialPort.close();
        } catch (Exception e) {
        }
        mSerialPort = null;
    }


    private class ReadThread extends Thread {

        private final InputStream mInputStream;

        public ReadThread(InputStream inputStream) {
            this.mInputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[512];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        psu.preparePacket(PacketSimpleUtil.subBytes(buffer, 0, size), cb);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    connected = false;
                    return;
                }
            }
        }
    }

    private class WriteThread extends Thread {

        private final OutputStream mOutputStream;
        private final LinkedBlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>(); //发送队列,线程安全！

        public WriteThread(OutputStream outputStream) {
            mOutputStream = outputStream;
        }

        public void sendData(byte[] bytes) {
            try {
                sendQueue.put(bytes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] bytes = sendQueue.take();
                    mOutputStream.write(bytes);
                    mOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    connected = false;
                    return;
                }
            }
        }
    }
}
