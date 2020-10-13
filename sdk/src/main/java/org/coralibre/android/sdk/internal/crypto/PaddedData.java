package org.coralibre.android.sdk.internal.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

public class PaddedData {
    public static final String RPI_INFO = "EN-RPI";
    public static final int PADDED_DATA_SIZE = 16;
    public static final int TIMESTAMP_POS = 12;
    private byte[] data = new byte[PADDED_DATA_SIZE];

    public PaddedData(ENInterval timestamp) {
        System.arraycopy(RPI_INFO.getBytes(StandardCharsets.UTF_8), 0, data, 0, RPI_INFO.length());
        System.arraycopy(timestamp.getBytes(), 0, data, TIMESTAMP_POS, ENInterval.INT_BYTES);
    }

    public PaddedData(byte[] rawPaddedData) {
        if(rawPaddedData.length != PADDED_DATA_SIZE)
            throw new InvalidParameterException("wrong rawPaddedData size");
        System.arraycopy(rawPaddedData, 0, data, 0, PADDED_DATA_SIZE);
    }

    public byte[] getData() {
        byte[] retVal = new byte[PADDED_DATA_SIZE];
        System.arraycopy(data, 0, retVal, 0, PADDED_DATA_SIZE);
        return retVal;
    }

    public boolean isRPIInfoValid() {
        byte[] extractedRpiInfo = new byte[RPI_INFO.length()];
        System.arraycopy(data, 0, extractedRpiInfo, 0, RPI_INFO.length());
        return (new String(extractedRpiInfo, StandardCharsets.UTF_8)).equals(RPI_INFO);
    }

    public ENInterval getInterval() {
        byte[] rawInterval = new byte[ENInterval.INT_BYTES];
        System.arraycopy(data, TIMESTAMP_POS, rawInterval, 0, ENInterval.INT_BYTES);
        return new ENInterval(rawInterval);
    }
}
