package org.coralibre.android.sdk.internal.crypto.ppcp;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class RollingProximityIdentifier {
    public static final int RPI_SIZE = 16;

    private final byte[] data = new byte[RPI_SIZE];
    private final ENNumber interval;

    public RollingProximityIdentifier(byte[] rawPRI, ENNumber interval) {
        if(rawPRI.length != RPI_SIZE) throw new InvalidParameterException("wrong raw rawRPI size");
        this.interval = new ENNumber(interval);
        System.arraycopy(rawPRI, 0, data, 0, RPI_SIZE);
    }

    public byte[] getData() {
        byte[] retVal = new byte[RPI_SIZE];
        System.arraycopy(data, 0, retVal, 0, RPI_SIZE);
        return retVal;
    }

    public ENNumber getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RollingProximityIdentifier that = (RollingProximityIdentifier) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
