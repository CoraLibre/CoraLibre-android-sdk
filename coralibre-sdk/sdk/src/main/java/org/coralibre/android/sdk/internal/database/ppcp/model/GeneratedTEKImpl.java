package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

public class GeneratedTEKImpl implements GeneratedTEK {

    private final ENNumber interval;
    private final byte[] key;

    public GeneratedTEKImpl(ENNumber interval, byte[] key) {
        this.interval =interval;
        this.key = key;
    }

    @Override
    public ENNumber getInterval() {
        return interval;
    }

    @Override
    public byte[] getKey() {
        return key;
    }
}
