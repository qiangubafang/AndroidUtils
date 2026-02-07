package org.tcshare.utils.bluetooth;

public interface IPacketUtil {
    public ValidPacket validPayload(byte[] recvBytes);

    public void preparePacket(byte[] bytes, IPacketReadyCallBack cb);

    public void resetPacket();

    public interface IPacketReadyCallBack {
        /**
         * 数据包准备好了
         *
         * @param bytes
         */
        void onPacketReady(byte[] bytes);
    }

}
