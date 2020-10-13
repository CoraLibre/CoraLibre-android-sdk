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
    private byte[] rpi;

    /** 4 bytes */
    private byte[] aem;


    public CapturedData(long captureTimestamp, byte rssi, byte[] rpi, byte[] aem) {
        this.captureTimestamp = captureTimestamp;
        this.enInterval = new ENInterval(captureTimestamp, true);
        this.rssi = rssi;
        this.rpi = rpi;
        this.aem = aem;
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

    public byte[] getRpi() {
        // TODO Return copy, not the byte array itself
        return rpi;
    }

    public byte[] getAem() {
        // TODO Return copy, not the byte array itself
        return aem;
    }
}
