package org.tcshare.utils.packet;

public interface IPacket {
    byte[] gen(byte[] content);
    void preparePacket(byte[] bytes, IPacketCallback cb);
}
