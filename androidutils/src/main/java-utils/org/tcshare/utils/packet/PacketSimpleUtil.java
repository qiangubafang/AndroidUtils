package org.tcshare.utils.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
/**
 * 数据包格式：
 * 包头	  长度	  内容	     校验	包尾
 * 1	  2	      1-N个字节	 2	    1
 *
 */
public class PacketSimpleUtil implements IPacket {
    private final byte header;
    private final byte tail;
    private static final int FIX_PRE_LEN = 3; // 包头	长度
    private static final int FIX_SUF_LEN = 3; // 校验	包尾
    private static final int FIX_MIN_LEN = FIX_PRE_LEN + FIX_SUF_LEN; // 最短长度
    private final String TAG = PacketSimpleUtil.class.getSimpleName();
    private final LinkedList<Byte> revBytesCache = new LinkedList<>(); // 接收缓存，环形缓冲，只操作头尾
    private final int maxLen; // 一个包最大长度
    private int packetLenType = 0;


    /**
     * 数据包格式：
     * 包头	  长度	  内容	     校验	包尾
     * 1	  2	      1-N个字节	 2	    1
     *
     * @param header
     * @param tail
     * @param maxLen
     * @param packetLenType 包长度类型， 0 ： 整个包长度。 1： 仅内容长度。 2： 负载的长度（即：长度后面所有字节的长度）
     */
    public PacketSimpleUtil(byte header, byte tail, int maxLen, int packetLenType) {
        this.header = header;
        this.tail = tail;
        this.maxLen = maxLen;
        this.packetLenType = packetLenType;
    }

    public PacketSimpleUtil(byte header, byte tail, int maxLen) {
        this.header = header;
        this.tail = tail;
        this.maxLen = maxLen;
        this.packetLenType = 0;
    }

    /**
     * 数据包格式：
     * 包头	长度	   内容	     校验	包尾
     * 1	  2	 1-N个字节	 2	    1
     * 包头:0x68
     * 长度：数据包的总长度
     * 包尾:0x16
     * 校验:CRC16 Modbus校验  从包头开始到校验字段之前
     * 使用大端序
     *
     * @param content
     * @return
     */
    public byte[] gen(byte[] content) {
        int totalLen = FIX_MIN_LEN + content.length;
        ByteBuffer payload = ByteBuffer.allocate(FIX_PRE_LEN + content.length);
        payload.put(header); // 包头
        if(packetLenType == 0) {
            payload.putShort((short) totalLen); // 长度
        }else if(packetLenType == 1){
            payload.putShort((short) content.length);
        }else if(packetLenType == 2){
            payload.putShort((short) (content.length + FIX_SUF_LEN));
        }
        payload.put(content); // 内容
        byte[] crc16 = cal(payload.array());

        ByteBuffer bb = ByteBuffer.allocate(totalLen);
        bb.put(payload.array());
        bb.put(crc16);
        bb.put(tail); // 包尾

        return bb.array();
    }


    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }


    private boolean validCRC(byte[] recvBytes) {
        byte[] prefix = new byte[recvBytes.length - FIX_SUF_LEN]; // 除去末尾的长度
        System.arraycopy(recvBytes, 0, prefix, 0, recvBytes.length - FIX_SUF_LEN);
        byte[] crc = new byte[2];
        System.arraycopy(recvBytes, recvBytes.length - FIX_SUF_LEN, crc, 0, 2);
        return Arrays.equals(cal(prefix), crc);
    }

    /**
     * 计算校验和
     * yxh
     *
     * @param bytes
     * @return
     */
    private static byte[] cal(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return new byte[]{(byte) ((CRC >> 8) & 0x000000FF), (byte) CRC};
    }

    /**
     * 对齐包头
     */
    private int alignPacketStart() {
        Iterator<Byte> it = revBytesCache.iterator();
        while (it.hasNext()) {
            Byte b = it.next();
            if (b == header) {// 匹配第一个字节, 预测下一个头
                if (it.hasNext()) { // 忽略第二个字节，和第三个字节， （长度）
                    byte highByte = it.next();
                    if (it.hasNext()) {
                        byte lowByte = it.next();
                        int packetSize = (highByte << 8 & 0xff00) | (lowByte & 0xff);
                        if (packetSize > maxLen || packetSize < FIX_MIN_LEN) { // 数据包太长或太短
                            return -1; // 错误
                        }
                        return 1; // 包头满足检测
                    }
                }
            } else {
                it.remove();
            }
        }
        return 0; // 0 未完成
    }

    public synchronized void preparePacket(byte[] bytes, IPacketCallback cb) {
        for (byte b : bytes) {
            revBytesCache.add(b);
        }

        int ret = alignPacketStart(); // 数据包包头 是匹配的
        while (ret == -1) {
            revBytesCache.removeFirst();
            ret = alignPacketStart();
        }

        if (revBytesCache.size() < FIX_MIN_LEN) { // 收到数据不足一个包头+包尾，继续等
            return;
        }

        int bytesSize = (revBytesCache.get(1) << 8 & 0xff00) | (revBytesCache.get(2) & 0xff);
        int packetSize = 0;
        if(packetLenType == 0) {
            packetSize = bytesSize;
        }else if(packetLenType == 1){
            packetSize = FIX_MIN_LEN + bytesSize; // 加上头尾
        }else if(packetLenType == 2){
            packetSize = FIX_PRE_LEN + bytesSize; // 加上头
        }
        if (revBytesCache.size() < packetSize) { // 接收到的包不完整，继续等;
            return;
        }

        // 提取第一个包
        byte[] packet = toPrimitives(revBytesCache.subList(0, packetSize).toArray(new Byte[0]));
        if (validCRC(packet)) { // CRC 有效则通知收到一个数据包
            byte[] payload = subBytes(packet, FIX_PRE_LEN, packet.length - FIX_MIN_LEN);
            cb.onPacketReady(payload);
        }

        // 长度满足了，无论是否有效，都从缓存里删除该数据包长度的数据
        for (int i = 0; i < packet.length; i++) {
            revBytesCache.removeFirst();
        }
    }


    /**
     * @param oBytes
     * @return
     */
    private static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];

        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }

        return bytes;
    }

    public synchronized void resetPacket() {
        revBytesCache.clear();
    }

}
