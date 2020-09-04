package org.coralibre.android.sdk.internal.crypto;

import java.security.InvalidParameterException;

public class AssociatedEncryptedMetadata {
    private final byte[] data = new byte[AssociatedMetadata.AEM_LENGTH];

    public AssociatedEncryptedMetadata(byte[] rawAEM) {
        if(rawAEM.length != AssociatedMetadata.AEM_LENGTH) throw new InvalidParameterException("wrong rawAEM size");
        System.arraycopy(rawAEM, 0, data, 0, AssociatedMetadata.AEM_LENGTH);
    }

    public byte[] getData() {
        byte[] retVal = new byte[AssociatedMetadata.AEM_LENGTH];
        System.arraycopy(data, 0, retVal, 0, AssociatedMetadata.AEM_LENGTH);
        return retVal;
    }
}
