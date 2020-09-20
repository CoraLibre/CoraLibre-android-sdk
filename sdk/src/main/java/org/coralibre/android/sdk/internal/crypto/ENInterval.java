package org.coralibre.android.sdk.internal.crypto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Objects;

public class ENInterval {
    public static final long MAX_UINT_32 = 4294967295L;
    public static final long UNIX_TIME_DEVIDER = 60*10; //10 minutes
    public static final int LONG_BYTES = 8;
    public static final int INT_BYTES = 4;

    private long val;

    public ENInterval(ENInterval enInterval) {
        this.val = enInterval.get();
    }

    public ENInterval(long rawENNumber) {
        set(rawENNumber, false);
    }

    public ENInterval(long input, boolean isUnixTime) {
        set(input, isUnixTime);
    }

    public ENInterval(byte[] rawENNumber) {
        if(rawENNumber.length != INT_BYTES)
            throw new InvalidParameterException("rawENNumber has wrong size");
        byte[] rawBuffer = new byte[LONG_BYTES];
        System.arraycopy(rawENNumber, 0, rawBuffer, 0, INT_BYTES);
        ByteBuffer buffer = ByteBuffer.wrap(rawBuffer);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        set(buffer.getLong(), false);
    }

    private void set(long input, boolean isUnixTime) {
        if(isUnixTime)
            input = input/UNIX_TIME_DEVIDER;

        if(input < 0 || MAX_UINT_32 < input)
            throw new InvalidParameterException("ENIntervalTimestamp out of bound: "
            + String.format("0x%X", input));
        val = input;
    }

    public long get() {
        return val;
    }

    public long getUnixTime() {
        return val * UNIX_TIME_DEVIDER;
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
