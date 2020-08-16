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

    private ENNumber interval;
    private byte[] key = new byte[TEK_LENGTH];


    public TemporaryExposureKey(ENNumber interval, byte[] value) {
        if(value.length < TEK_LENGTH) throw new InvalidParameterException("tek size not 16bytes");
        this.interval = getMidnight(interval);
        System.arraycopy(value, 0, key, 0, TEK_LENGTH);
    }

    public TemporaryExposureKey(long timestamp, byte[] rawKey) {
        this(new ENNumber(timestamp), rawKey);
    }

    public ENNumber getInterval() {
        return interval;
    }

    public byte[] getKey() {
        byte[] retVal = new byte[TEK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, TEK_LENGTH);
        return retVal;
    }

    public static ENNumber getMidnight(ENNumber enNumber) {
        return new ENNumber( ((long)(enNumber.get() / TEK_ROLLING_PERIOD)) * TEK_ROLLING_PERIOD);
    }

    public static long getMidnight(long rawENNumber) {
        return ((rawENNumber/ TEK_ROLLING_PERIOD) * TEK_ROLLING_PERIOD);
    }
}
