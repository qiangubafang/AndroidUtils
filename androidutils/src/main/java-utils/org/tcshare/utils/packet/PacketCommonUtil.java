package org.tcshare.utils.packet;

import android.util.Log;

import org.tcshare.utils.HexDump;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 长度为大端序，java默认为大端序！
 * 数据包格式：
 * 包头	  长度	  内容	     包尾（可选）
 * X	  X	      1-N个字节	  X
 * <p>
 * 注意：这里没有CRC的选项，因为目前的项目，什么样的格式都有。
 * 有些为了自己的方便，自定义的情况太多。 通用格式太难。
 * 关于CRC（大小端），使用CRC16，crc32 类里的计算方法，计算完后，再放进来。
 */
public class PacketCommonUtil implements IPacket {
    private boolean DEBUG = false;
    private final String TAG = PacketSimpleUtil.class.getSimpleName();
    protected final byte[] header;
    protected final byte[] tail;
    protected final int lenBytes; // 长度占用几个字节
    protected final int FIX_PRE_LEN; // 包头	长度
    protected final int FIX_SUF_LEN; // 包尾
    protected final int FIX_MIN_LEN; // 最短长度

    protected final int maxLen; // 一个包最大长度


    private final LinkedList<Byte> revBytesCache = new LinkedList<>(); // 接收缓存，环形缓冲，只操作头尾
    private boolean bigEndian = true; // 默认大端序

    protected int packetLenType = 0;


    /**
     * 数据包格式：
     * 包头	  长度	  内容	     包尾
     * X	  X	      X个字节	  X
     *
     * @param header
     * @param tail
     * @param lenBytes      长度占用几个字节[1、2、4]三个选项
     * @param maxLen        单个包的最大长度
     * @param packetLenType 包长度类型，
     *                      0： 整个包长度，含头尾
     *                      1： 仅内容长度。
     *                      2： 负载的长度。 长度字节 后面的长度，含包尾（如果有的话）
     *                      3:  除去包头，包尾的长度
     */
    public PacketCommonUtil(byte[] header, byte[] tail, int lenBytes, int maxLen, int packetLenType, boolean bigEndian) {
        this.header = header;
        this.tail = tail;
        this.lenBytes = lenBytes;
        this.maxLen = maxLen;
        this.packetLenType = packetLenType;
        this.FIX_PRE_LEN = header.length + lenBytes;
        this.FIX_SUF_LEN = tail.length;
        this.FIX_MIN_LEN = FIX_PRE_LEN + FIX_SUF_LEN;
        this.bigEndian = bigEndian;
    }

    public PacketCommonUtil(byte[] header, byte[] tail, int lenBytes, int maxLen, int packetLenType) {
        this(header, tail, lenBytes, maxLen, packetLenType, true);
    }

    /**
     * 数据包格式：
     * 包头	  长度	  内容	     包尾
     * X	  X	      X个字节	  X
     *
     * @param content
     * @return
     */
    public byte[] gen(byte[] content) {
        int totalLen = FIX_MIN_LEN + content.length;
        ByteBuffer pkBB = ByteBuffer.allocate(totalLen);
        pkBB.put(header); // 包头
        if (packetLenType == 0) { // 长度
            pkBB.put(calcLenBytes(totalLen));
        } else if (packetLenType == 1) {
            pkBB.put(calcLenBytes(content.length));
        } else if (packetLenType == 2) {
            pkBB.put(calcLenBytes((content.length + FIX_SUF_LEN)));
        } else if (packetLenType == 3) {
            pkBB.put(calcLenBytes((content.length + lenBytes)));
        }
        pkBB.put(content); // 内容
        pkBB.put(tail); // 包尾

        return pkBB.array();
    }


    private byte[] calcLenBytes(int len) {
        ByteBuffer bb = ByteBuffer.allocate(lenBytes).order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        if (lenBytes == 1) {
            bb.put((byte) (len & 0xFF));
        } else if (lenBytes == 2) {
            bb.putShort((short) len);
        } else {
            bb.putInt(len);
        }
        return bb.array();
    }

    private int calcLen(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        if (lenBytes == 1) {
            return bb.get();
        } else if (lenBytes == 2) {
            return bb.getShort();
        } else {
            return bb.getInt();
        }
    }


    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }


    /**
     * 对齐包头
     */
    private int alignPacketStart() {
        Iterator<Byte> it = revBytesCache.iterator();
        if (revBytesCache.size() < FIX_MIN_LEN) {
            return 0;
        }
        byte[] tmpHeader = new byte[header.length];
        for (int i = 0; i < tmpHeader.length; i++) {
            tmpHeader[i] = it.next();
        }
        if (!Arrays.equals(tmpHeader, header)) {
            return -1;
        }

        byte[] lengthBytes = new byte[lenBytes];
        for (int i = 0; i < lenBytes; i++) {
            lengthBytes[i] = it.next();
        }
        int totalLen = getTotalLen(lengthBytes);

        if (totalLen > maxLen || totalLen < FIX_MIN_LEN) {
            return -1;
        }

        if (revBytesCache.size() >= totalLen) {
            return 1; // 包头，以及长度符合要求了
        } else {
            return 0; // 0 未完成
        }
    }

    private int getTotalLen(byte[] lengthBytes) {
        int packetSize = calcLen(lengthBytes);


        int totalLen = 0;
        if (packetLenType == 0) { // 长度
            totalLen = packetSize;
        } else if (packetLenType == 1) {
            totalLen = FIX_MIN_LEN + packetSize;
        } else if (packetLenType == 2) {
            totalLen = FIX_PRE_LEN + packetSize;
        } else if (packetLenType == 3) {
            totalLen = header.length + tail.length + packetSize;
        }


        return totalLen;
    }

    public synchronized void preparePacket(byte[] bytes, IPacketCallback cb) {
        if(DEBUG) Log.e(TAG, "recv bytes:" + HexDump.dumpHexString(bytes));
        for (byte b : bytes) {
            revBytesCache.add(b);
        }

        int ret = alignPacketStart();  // 对齐包头，并检验长度
        while (ret == -1) {
            revBytesCache.removeFirst();
            ret = alignPacketStart();
        }

        if (ret != 1) { // 包头，以及长度不符合要求
            return;
        }

        byte[] lengthBytes = new byte[lenBytes];
        for (int i = 0; i < lenBytes; i++) {
            lengthBytes[i] = revBytesCache.get(header.length + i);
        }
        int packetSize = getTotalLen(lengthBytes);

        // 提取第一个包，包头及长度符合
        byte[] packet = getPacketAndRemove(revBytesCache, packetSize);
        if (tail.length > 0) {
            byte[] tmpTail = subBytes(packet, packetSize - tail.length, tail.length);
            if (Arrays.equals(tail, tmpTail)) { // 匹配的包尾
                byte[] payload = subBytes(packet, FIX_PRE_LEN, packet.length - FIX_MIN_LEN);
                cb.onPacketReady(payload);
            }
        } else {
            byte[] payload = subBytes(packet, FIX_PRE_LEN, packet.length - FIX_MIN_LEN);
            cb.onPacketReady(payload);
        }

    }

    /**
     * 抽取第一个包
     *
     * @param revBytesCache
     * @param packetSize
     * @return
     */
    private static byte[] getPacketAndRemove(LinkedList<Byte> revBytesCache, int packetSize) {
        byte[] ret = new byte[packetSize];
        for (int i = 0; i < packetSize; i++) {
            Byte b = revBytesCache.pop();
            ret[i] = b;
        }
        return ret;
    }



    public synchronized void resetPacket() {
        revBytesCache.clear();
    }

    public boolean isDEBUG() {
        return DEBUG;
    }

    public void setDEBUG(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }
}
