package org.coralibre.android.sdk.internal.database.model;


import org.coralibre.android.sdk.internal.crypto.ENInterval;


public class GeneratedTEK {


    private final ENInterval interval;

    private final byte[] key;



    public GeneratedTEK(ENInterval interval, byte[] key) {
        this.interval =interval;
        this.key = key;
    }


    public ENInterval getInterval() {
        return interval;
    }

    public byte[] getKey() {
        return key;
    }
}
