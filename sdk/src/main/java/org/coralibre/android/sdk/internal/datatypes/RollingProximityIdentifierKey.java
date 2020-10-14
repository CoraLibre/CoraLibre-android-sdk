package org.coralibre.android.sdk.internal.datatypes;

import java.security.InvalidParameterException;

import static org.coralibre.android.sdk.internal.EnFrameworkConstants.RPIK_LENGTH;

public class RollingProximityIdentifierKey {

    private final byte[] key = new byte[RPIK_LENGTH];

    public RollingProximityIdentifierKey(final byte[] value) {
        if(value.length != RPIK_LENGTH) throw new InvalidParameterException("size not 16bytes");
        System.arraycopy(value, 0, key, 0, RPIK_LENGTH);
    }

    public byte[] getKey() {
        byte[] retVal = new byte[RPIK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, RPIK_LENGTH);
        return retVal;
    }
}
