package org.tcshare.utils.serial;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG = SerialPort.class.getSimpleName();

    static {
        System.loadLibrary("tc_serialport_n");
    }

    /**
     * 不要修改 mFd 变量名 : native close() 方法在用
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    /**
     *
     * @param path
     * @param baudrate
     * @param flags
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPort(String path, int baudrate, int flags) throws SecurityException, IOException {
        mFd = open(path, baudrate, flags);
        if (mFd == null) {
            Log.d(TAG, "native open returns null");
            throw new IOException();
        } else{
            Log.d(TAG, "native open returns ok");
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    /**
     * 打开串口
     * @return
     */
    public native static FileDescriptor open(String path, int baudrate, int flags);
    public native void close();

}
