package org.tcshare.utils.bluetooth;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Commander {
    private final byte header;
    private final byte subHeader;
    private final byte tail;
    public int FIX_PRE_LEN = 5; // 包头	长度	 子包头	类型
    public int FIX_SUF_LEN = 3; // 校验	包尾
    private final String TAG = Commander.class.getSimpleName();
    private final LinkedList<Byte> revBytesCache = new LinkedList<>(); // 接收缓存，环形缓冲，只操作头尾
    private int maxLen = 512; // 一个包最大长度


    /**
     * 数据包格式：
     * 包头	长度	 子包头	类型	  内容	     校验	包尾
     * 1	    2	 1	    1  	  1-N个字节	 2	    1
     *
     * @param header
     * @param subHeader
     * @param tail
     * @param maxLen
     */
    public Commander(byte header, byte subHeader, byte tail, int maxLen) {
        this.header = header;
        this.subHeader = subHeader;
        this.tail = tail;
        this.maxLen = maxLen;
    }

    /**
     * 数据包格式：
     * 包头	长度	 子包头	类型	  内容	     校验	包尾
     * 1	    2	 1	    1  	  1-N个字节	 2	    1
     * <p>
     * 包头:0x68
     * 长度：数据包的总长度
     * 子包头:0x55
     * 包尾:0x16
     * 校验:CRC16 Modbus校验  从包头开始到校验字段之前
     * 使用大端序
     *
     * @param type
     * @param content
     * @return
     */
    public byte[] gen(byte type, byte[] content) {
        int totalLen = FIX_PRE_LEN + FIX_SUF_LEN + content.length;
        ByteBuffer payload = ByteBuffer.allocate(FIX_PRE_LEN + content.length);
        payload.put(header); // 包头
        payload.putShort((short) totalLen); // 长度
        payload.put(subHeader); // 子包头
        payload.put(type); // 类型
        payload.put(content); // 内容
        byte[] crc16 = cal(payload.array());

        ByteBuffer bb = ByteBuffer.allocate(totalLen);
        bb.put(payload.array());
        bb.put(crc16);
        bb.put(tail); // 包尾

        return bb.array();
    }

    /**
     * 检查返回的数据是否成功，
     *
     * @param recvBytes
     * @return 第一个字节代表是否成功，第二个字节数组代表负载
     */
    public ValidPacket validPayload(byte[] recvBytes) {
        try {
            if (recvBytes == null || recvBytes.length < (FIX_PRE_LEN + FIX_SUF_LEN)) { // 收到空数据， 或者小于最短长度
                return new ValidPacket(false, (byte) -1, new byte[0]);
            }
            if (!validCRC(recvBytes)) { // 校验和不对
                Log.e(TAG, "校验和错误");
                return new ValidPacket(false, (byte) -1, new byte[0]);
            }
            byte type = recvBytes[4];// 类型
            byte[] payload = subBytes(recvBytes, 5, recvBytes.length - FIX_PRE_LEN - FIX_SUF_LEN);
            return new ValidPacket(true, recvBytes[4], payload);

        } catch (Exception e) {
            e.printStackTrace();
            return new ValidPacket(false, (byte) -1, new byte[0]); // 解析异常，比如新增，修改后字节不匹配等
        }
    }


    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }


    private boolean validCRC(byte[] recvBytes) {
        byte[] prefix = new byte[recvBytes.length - FIX_SUF_LEN]; // 出去末尾的长度
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
                        if (packetSize > maxLen) { // 数据包太长
                            return -1; // 错误
                        }
                        if (it.hasNext()) { // 第四个字节 是子头
                            if (it.next() == subHeader) {
                                return 1; // 包头初步满足检测
                            } else {
                                return alignPacketStart();
                            }
                        }
                    }
                }
            } else {
                it.remove();
            }
        }
        return 0; // 0 未完成
    }

    public synchronized void preparePacket(byte[] bytes, PacketReadyCallBack cb) {
        for (byte b : bytes) {
            revBytesCache.add(b);
        }

        int ret = alignPacketStart(); // 数据包前4个字节（包头）是匹配的
        while (ret == -1) {
            revBytesCache.removeFirst();
            ret = alignPacketStart();
        }

        if (revBytesCache.size() < FIX_PRE_LEN + FIX_SUF_LEN) { // 收到数据不足一个包头+包尾，继续等
            return;
        }

        int packetSize = (revBytesCache.get(1) << 8 & 0xff00) | (revBytesCache.get(2) & 0xff);
        if (revBytesCache.size() < packetSize) { // 接收到的包不完整，继续等
            return;
        }

        // 提取第一个包
        byte[] packet = toPrimitives(revBytesCache.subList(0, packetSize).toArray(new Byte[0]));
        if (validCRC(packet)) {
            cb.onPacketReady(packet);
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

    public interface PacketReadyCallBack {
        /**
         * 数据包准备好了
         *
         * @param bytes
         */
        void onPacketReady(byte[] bytes);
    }
}
