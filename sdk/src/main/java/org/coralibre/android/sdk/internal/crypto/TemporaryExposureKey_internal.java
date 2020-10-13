package org.coralibre.android.sdk.internal.crypto;

import java.security.InvalidParameterException;

// TODO maybe rename again, but for now I want to have a unique class name to avoid confusion

public class TemporaryExposureKey_internal {
    public static final int TEK_ROLLING_PERIOD = 144; //defined as 10min units

    public final long MAX_UINT_32 = 4294967295L;
    public static final int TEK_LENGTH = 16; //defined in byte

    private ENInterval interval;
    private byte[] key = new byte[TEK_LENGTH];


    public TemporaryExposureKey_internal(ENInterval interval, byte[] value) {
        if(value.length < TEK_LENGTH) throw new InvalidParameterException("tek size not 16bytes");
        this.interval = getMidnight(interval);
        System.arraycopy(value, 0, key, 0, TEK_LENGTH);
    }

    public TemporaryExposureKey_internal(long timestamp, byte[] rawKey) {
        this(new ENInterval(timestamp), rawKey);
    }

    public ENInterval getInterval() {
        return interval;
    }

    public byte[] getKey() {
        byte[] retVal = new byte[TEK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, TEK_LENGTH);
        return retVal;
    }

    public static ENInterval getMidnight(ENInterval enInterval) {
        return new ENInterval( ((long)(enInterval.get() / TEK_ROLLING_PERIOD)) * TEK_ROLLING_PERIOD);
    }

    public static long getMidnight(long rawENNumber) {
        return ((rawENNumber/ TEK_ROLLING_PERIOD) * TEK_ROLLING_PERIOD);
    }
}
