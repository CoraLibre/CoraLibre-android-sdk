package org.coralibre.android.sdk.internal.datatypes;

import org.coralibre.android.sdk.internal.EnFrameworkConstants;

import java.security.InvalidParameterException;

public class AssociatedEncryptedMetadataKey {

    private final byte[] key = new byte[EnFrameworkConstants.AEMK_LENGTH];

    public AssociatedEncryptedMetadataKey(final byte[] value) {
        if(value.length != EnFrameworkConstants.AEMK_LENGTH) throw new InvalidParameterException("size not 16bytes");
        System.arraycopy(value, 0, key, 0, EnFrameworkConstants.AEMK_LENGTH);
    }

    public byte[] getKey() {
        byte[] retVal = new byte[EnFrameworkConstants.AEMK_LENGTH];
        System.arraycopy(key, 0, retVal, 0, EnFrameworkConstants.AEMK_LENGTH);
        return retVal;
    }
}
