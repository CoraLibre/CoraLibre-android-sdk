package org.coralibre.android.sdk.internal.crypto.ppcp;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;

public class PaddedData {
    public static final String RPI_INFO = "EN-RPI";
    public static final int PADDED_DATA_SIZE = 16;
    public static final int TIMESTAMP_POS = 12;
    private byte[] data = new byte[PADDED_DATA_SIZE];

    public PaddedData(ENNumber timestamp) {
        System.arraycopy(RPI_INFO.getBytes(StandardCharsets.UTF_8), 0, data, 0, RPI_INFO.length());
        System.arraycopy(timestamp.getBytes(), 0, data, TIMESTAMP_POS, ENNumber.INT_BYTES);
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
        byte[] extracted_rpiinfo = new byte[RPI_INFO.length()];
        System.arraycopy(data, 0, extracted_rpiinfo, 0, RPI_INFO.length());
        return (new String(extracted_rpiinfo, StandardCharsets.UTF_8)).equals(RPI_INFO);
    }

    public ENNumber getTimestamp() {
        byte[] rawTimestamp = new byte[ENNumber.INT_BYTES];
        System.arraycopy(data, TIMESTAMP_POS, rawTimestamp, 0, ENNumber.INT_BYTES);
        return new ENNumber(rawTimestamp);
    }
}