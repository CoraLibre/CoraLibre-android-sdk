package org.coralibre.android.sdk.internal.datatypes;

import org.coralibre.android.sdk.internal.EnFrameworkConstants;

import java.security.InvalidParameterException;

public class AssociatedEncryptedMetadata {
    private final byte[] data = new byte[EnFrameworkConstants.AEM_LENGTH];

    public AssociatedEncryptedMetadata(final byte[] rawAEM) {
        if(rawAEM.length != EnFrameworkConstants.AEM_LENGTH) throw new InvalidParameterException("wrong rawAEM size");
        System.arraycopy(rawAEM, 0, data, 0, EnFrameworkConstants.AEM_LENGTH);
    }

    public byte[] getData() {
        byte[] retVal = new byte[EnFrameworkConstants.AEM_LENGTH];
        System.arraycopy(data, 0, retVal, 0, EnFrameworkConstants.AEM_LENGTH);
        return retVal;
    }
}
