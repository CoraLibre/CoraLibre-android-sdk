package org.coralibre.android.sdk.internal.datatypes;

import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Objects;

public class ENInterval {
    public static final long MAX_UINT_32 = 4294967295L;
    public static final int LONG_BYTES = 8;
    public static final int INT_BYTES = 4;


    private final long val;


    public ENInterval(final ENInterval enInterval) {
        this.val = enInterval.get();
    }

    public ENInterval(final long rawENNumber) {
        val = toEnIntervalNumberChecked(rawENNumber, false);
    }

    public ENInterval(final long input, final boolean isUnixTime) {
        val = toEnIntervalNumberChecked(input, isUnixTime);
    }

    public ENInterval(final byte[] rawENNumber) {
        if(rawENNumber.length != INT_BYTES)
            throw new InvalidParameterException("rawENNumber has wrong size");
        byte[] rawBuffer = new byte[LONG_BYTES];
        System.arraycopy(rawENNumber, 0, rawBuffer, 0, INT_BYTES);
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        val = toEnIntervalNumberChecked(buffer.getLong(), false);
    }

    private long toEnIntervalNumberChecked(long input, boolean isUnixTime) {
        long result = input;
        if(isUnixTime)
            result = ENIntervalUtil.intervalNumberFromUnixTimestamp(input);

        if(result < 0 || MAX_UINT_32 < result)
            throw new InvalidParameterException("ENIntervalTimestamp out of bound: "
            + String.format("0x%X", result));
        return result;
    }

    public long get() {
        return val;
    }

    public long getUnixTime() {
        return ENIntervalUtil.intervalNumberToUnixTimestamp(val);
    }

    public byte[] getBytes() {
        // This has to output the value as Little endian
        ByteBuffer buffer = ByteBuffer.allocate(LONG_BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(val);
        byte[] retVal = new byte[INT_BYTES];
        System.arraycopy(buffer.array(), 0, retVal, 0, INT_BYTES);
        return retVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ENInterval enInterval = (ENInterval) o;
        return val == enInterval.val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }

    @Override
    public String toString() {
        return Long.toString(val);
    }
}
