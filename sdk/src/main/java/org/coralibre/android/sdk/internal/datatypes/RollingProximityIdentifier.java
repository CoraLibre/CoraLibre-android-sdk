package org.coralibre.android.sdk.internal.datatypes;

import java.security.InvalidParameterException;
import java.util.Arrays;

import static org.coralibre.android.sdk.internal.EnFrameworkConstants.RPI_LENGTH;

public class RollingProximityIdentifier {

    private final byte[] data = new byte[RPI_LENGTH];
    private final ENInterval interval;


    public RollingProximityIdentifier(final byte[] rawRPI, final ENInterval interval) {
        if(rawRPI.length != RPI_LENGTH) throw new InvalidParameterException("wrong rawRPI size");
        this.interval = new ENInterval(interval);
        System.arraycopy(rawRPI, 0, data, 0, RPI_LENGTH);
    }

    public byte[] getData() {
        byte[] retVal = new byte[RPI_LENGTH];
        System.arraycopy(data, 0, retVal, 0, RPI_LENGTH);
        return retVal;
    }

    public ENInterval getInterval() {
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
