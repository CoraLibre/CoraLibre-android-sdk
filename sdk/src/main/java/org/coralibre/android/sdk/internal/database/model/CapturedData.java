package org.coralibre.android.sdk.internal.database.model;


import org.coralibre.android.sdk.internal.crypto.ENInterval;

public class CapturedData {


    /**
     *  Milliseconds since Epoch.
     */
    private final long captureTimestamp;

    /**
     * the 10-minute interval
     */
    private final ENInterval enInterval;

    /** in dB */
    private byte rssi;

    /** 16 bytes */
    private byte[] payload;

    public CapturedData(long captureTimestamp, byte rssi, byte[] payload) {
        this.captureTimestamp = captureTimestamp;
        this.enInterval = new ENInterval(captureTimestamp, true);
        this.rssi = rssi;
        this.payload = payload;
    }

    public Long getCaptureTimestamp() {
        return captureTimestamp;
    }

    public ENInterval getEnInterval() {
        return enInterval;
    }

    public byte getRssi() {
        return rssi;
    }

    public byte[] getPayload() {
        return payload;
    }
}
