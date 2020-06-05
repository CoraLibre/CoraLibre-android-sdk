package org.coralibre.android.sdk.internal.crypto.ppcp;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

public class RollingProximityIdentifier {
    public static final int RPI_SIZE = 16;

    private static final byte[] data = new byte[RPI_SIZE];

    public RollingProximityIdentifier(byte[] rawPRI) {
        if(rawPRI.length != RPI_SIZE) throw new InvalidParameterException("wrong raw rawRPI size");
        System.arraycopy(rawPRI, 0, data, 0, RPI_SIZE);
    }

    public byte[] getData() {
        byte[] retVal = new byte[RPI_SIZE];
        System.arraycopy(data, 0, retVal, 0, RPI_SIZE);
        return retVal;
    }
}
