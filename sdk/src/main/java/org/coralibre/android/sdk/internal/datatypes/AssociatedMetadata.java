package org.coralibre.android.sdk.internal.datatypes;

import java.security.InvalidParameterException;

import static org.coralibre.android.sdk.internal.EnFrameworkConstants.AEM_LENGTH;

public class AssociatedMetadata {
    private static final int VERSIONING_BYTE = 0;
    private static final int POWERLEVEL_BYTE = 1;
    private static final int MAJOR_BIT_POS = 6;
    private static final int MINOR_BIT_POS = 4;


    private final byte[] data = new byte[AEM_LENGTH];

    public AssociatedMetadata(final int majorVersion, final int minorVersion, final int powerLevel) {
        if(majorVersion < 0 || majorVersion >= 4)
            throw new InvalidParameterException("Major version out of bound");
        if(minorVersion < 0 || minorVersion >= 4)
            throw new InvalidParameterException("Minor version out of bound");
        if(powerLevel < -127 || powerLevel > 127)
            throw new InvalidParameterException("Power level out of bound");

        data[VERSIONING_BYTE] = (byte)(((majorVersion & 3) << MAJOR_BIT_POS)
                | ((minorVersion & 3) << MINOR_BIT_POS));
        data[POWERLEVEL_BYTE] = (byte) powerLevel;
    }

    public AssociatedMetadata(final byte[] rawAM) {
        if(rawAM.length != AEM_LENGTH) throw new InvalidParameterException("rawAEM not the right length");
        System.arraycopy(rawAM, 0, data, 0, AEM_LENGTH);
    }

    public int getMajorVersion() {
        return (data[VERSIONING_BYTE] >> MAJOR_BIT_POS) & 3;
    }

    public int getMinorVersion() {
        return (data[VERSIONING_BYTE] >> MINOR_BIT_POS) & 3;
    }

    public int getTransmitPowerLevel() {
        return data[POWERLEVEL_BYTE];
    }

    public byte[] getData() {
        byte[] retVal = new byte[AEM_LENGTH];
        System.arraycopy(data, 0, retVal, 0, AEM_LENGTH);
        return retVal;
    }
}
