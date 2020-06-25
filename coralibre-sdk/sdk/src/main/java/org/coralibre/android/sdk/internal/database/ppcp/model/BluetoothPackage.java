package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

public class BluetoothPackage {
    private ENNumber interval; // TODO ENNumber ist part of crypto

    /** in db */
    private byte rxPower;

    /** 16 bytes */
    private byte[] payload;

    public BluetoothPackage(ENNumber interval, byte rxPower, byte[] payload) {
        this.interval = interval;
        this.rxPower = rxPower;
        this.payload = payload;
    }

    public ENNumber getInterval() {
        return interval;
    }

    public byte getRxPower() {
        return rxPower;
    }

    public byte[] getPayload() {
        return payload;
    }
}
