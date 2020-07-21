package org.coralibre.android.sdk.internal.database.ppcp.model;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

public class CapturedData {



    private final long captureTimestamp; // in milliseconds since Epoch
    private final ENNumber enNumber;

    /** in dB */
    private byte rssi;

    /** 16 bytes */
    private byte[] payload;

    public CapturedData(long captureTimestamp, byte rssi, byte[] payload) {
        this.captureTimestamp = captureTimestamp;
        this.enNumber = new ENNumber(captureTimestamp, true);
        this.rssi = rssi;
        this.payload = payload;
    }

    public Long getCaptureTimestamp() {
        return captureTimestamp;
    }

    public ENNumber getEnNumber() {
        return enNumber;
    }

    public byte getRssi() {
        return rssi;
    }

    public byte[] getPayload() {
        return payload;
    }
}
