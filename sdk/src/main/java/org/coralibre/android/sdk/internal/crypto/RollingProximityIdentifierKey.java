package org.coralibre.android.sdk.internal.crypto;

import java.security.InvalidParameterException;
import java.security.MessageDigest;

public class RollingProximityIdentifierKey {
    public static final int RPIK_LENGTH = 16; // unit is bytes

    private final byte[] key = new byte[RPIK_LENGTH];

    public RollingProximityIdentifierKey(byte[] value) {
        if(value.length != RPIK_LENGTH) throw new InvalidParameterException("size not 16bytes");
        System.arraycopy(value, 0, key, 0, RPIK_LENGTH);
    }

    public byte[] getKey() {
        byte[] retVal = new byte[RPIK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, RPIK_LENGTH);
        return retVal;
    }
}
