package org.coralibre.android.sdk.internal.database.ppcp.models;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;

public class GeneratedTEK {
    private final TemporaryExposureKey tek;

    public GeneratedTEK(TemporaryExposureKey key) {
        this.tek = key;
    }

    public GeneratedTEK(byte[] key, ENNumber interval) {
        this.tek = new TemporaryExposureKey(interval, key);
    }

    public ENNumber getInterval() {
        return tek.getInterval();
    }

    public byte[] getRawKey() {
        return tek.getKey();
    }

    public TemporaryExposureKey getTek() {
        return tek;
    }
}
