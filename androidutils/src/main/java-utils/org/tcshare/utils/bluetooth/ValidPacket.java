package org.tcshare.utils.bluetooth;

public class ValidPacket {
    public boolean valid;
    public byte type;
    public byte[] payload;

    public ValidPacket(boolean valid, byte type, byte[] payload) {
        this.valid = valid;
        this.type = type;
        this.payload = payload;
    }
}
