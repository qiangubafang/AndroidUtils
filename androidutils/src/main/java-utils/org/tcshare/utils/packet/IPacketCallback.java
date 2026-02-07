package org.tcshare.utils.packet;

public interface IPacketCallback {

    /**
     * 数据包准备好了
     *
     * @param payload
     */
    void onPacketReady(byte[] payload);
}
