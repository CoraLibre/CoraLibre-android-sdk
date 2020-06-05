package org.coralibre.android.sdk.internal.crypto.ppcp;

import java.security.InvalidParameterException;

public class AssociatedEncryptedMetadataKey {
    public static final int AEMK_LENGTH = 16; // unit is bytes

    private final byte[] key = new byte[AEMK_LENGTH];

    public AssociatedEncryptedMetadataKey(byte[] value) {
        if(value.length != AEMK_LENGTH) throw new InvalidParameterException("size not 16bytes");
        System.arraycopy(value, 0, key, 0, AEMK_LENGTH);
    }

    public byte[] getKey() {
        byte[] retVal = new byte[AEMK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, AEMK_LENGTH);
        return retVal;
    }
}
