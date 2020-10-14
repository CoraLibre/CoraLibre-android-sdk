package org.coralibre.android.sdk.internal.datatypes;

import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;

import java.security.InvalidParameterException;

import static org.coralibre.android.sdk.internal.EnFrameworkConstants.TEK_LENGTH;

// TODO maybe rename again, but for now I want to have a unique class name to avoid confusion


public class TemporaryExposureKey_internal {

    private final ENInterval interval;
    private final byte[] key = new byte[TEK_LENGTH];

    public TemporaryExposureKey_internal(final ENInterval interval, final byte[] value) {
        if(value.length < TEK_LENGTH) throw new InvalidParameterException("tek size not 16bytes");
        this.interval = ENIntervalUtil.getMidnight(interval);
        System.arraycopy(value, 0, key, 0, TEK_LENGTH);
    }

    public TemporaryExposureKey_internal(final long timestamp, final byte[] rawKey) {
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
}
