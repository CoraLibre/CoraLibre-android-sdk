package org.coralibre.android.sdk.internal.database.ppcp.model;

public class CapturedData {
    private long captureTimestamp; // in milliseconds since Epoch

    /** in dB */
    private byte rssi;

    /** 16 bytes */
    private byte[] payload;

    public CapturedData(long captureTimestamp, byte rssi, byte[] payload) {
        this.captureTimestamp = captureTimestamp;
        this.rssi = rssi;
        this.payload = payload;
    }

    public Long getCaptureTimestamp() {
        return captureTimestamp;
    }

    public byte getRssi() {
        return rssi;
    }

    public byte[] getPayload() {
        return payload;
    }
}
