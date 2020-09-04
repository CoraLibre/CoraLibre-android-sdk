package org.coralibre.android.sdk.internal.database.model;


import org.coralibre.android.sdk.internal.crypto.ENNumber;


public class GeneratedTEK {


    private final ENNumber interval;

    private final byte[] key;



    public GeneratedTEK(ENNumber interval, byte[] key) {
        this.interval =interval;
        this.key = key;
    }


    public ENNumber getInterval() {
        return interval;
    }

    public byte[] getKey() {
        return key;
    }
}