package org.coralibre.android.sdk.internal.crypto.ppcp;

import android.util.Pair;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class TemporaryExposureKey {
    public static final int TEK_ROLLING_PERIOD = 144; //defined as 10min units

    public final long MAX_UINT_32 = 4294967295L;
    public static final int TEK_LENGTH = 16; //defined in byte

    private ENNumber timestamp;
    private byte[] key = new byte[TEK_LENGTH];


    public TemporaryExposureKey(ENNumber timestamp, byte[] value) {
        if(value.length < TEK_LENGTH) throw new InvalidParameterException("tek size not 16bytes");
        this.timestamp = new ENNumber(((long)(timestamp.get() / TEK_ROLLING_PERIOD)) * TEK_ROLLING_PERIOD);
        System.arraycopy(value, 0, key, 0, TEK_LENGTH);
    }

    public TemporaryExposureKey(Pair<Long, byte[]> rawKey) {
        this(new ENNumber(rawKey.first), rawKey.second);
    }

    public ENNumber getTimestamp() {
        return timestamp;
    }

    public byte[] getKey() {
        byte[] retVal = new byte[TEK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, TEK_LENGTH);
        return retVal;
    }
}
