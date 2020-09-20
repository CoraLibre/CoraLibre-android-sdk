package org.coralibre.android.sdk.internal.crypto;

import java.security.InvalidParameterException;

import static org.coralibre.android.sdk.internal.crypto.AssociatedMetadata.AEM_LENGTH;
import static org.coralibre.android.sdk.internal.crypto.RollingProximityIdentifier.RPI_LENGTH;

public class BluetoothPayload {
    public static final int PAYLOAD_LENGTH = RPI_LENGTH + AEM_LENGTH;
    private RollingProximityIdentifier rpi;
    private AssociatedEncryptedMetadata aem;

    public BluetoothPayload(byte[] rawPayload, ENInterval interval) {
        if(rawPayload.length != RPI_LENGTH + AEM_LENGTH)
            throw new InvalidParameterException("wrong payload size");
        byte[] rawRpi = new byte[RPI_LENGTH];
        byte[] rawAem = new byte[AEM_LENGTH];
        System.arraycopy(rawPayload, 0, rawRpi, 0, RPI_LENGTH);
        System.arraycopy(rawPayload, RPI_LENGTH, rawAem, 0, AEM_LENGTH);
        this.rpi = new RollingProximityIdentifier(rawRpi, interval);
        this.aem = new AssociatedEncryptedMetadata(rawAem);
    }

    public BluetoothPayload(RollingProximityIdentifier rpi, AssociatedEncryptedMetadata aem) {
        this.rpi = rpi;
        this.aem = aem;
    }

    public RollingProximityIdentifier getRpi() {
        return rpi;
    }

    public AssociatedEncryptedMetadata getAem() {
        return aem;
    }

    public ENInterval getInterval() {
        return rpi.getInterval();
    }

    public byte[] getRawPayload() {
        byte[] payload = new byte[RPI_LENGTH + AEM_LENGTH];
        System.arraycopy(rpi.getData(), 0, payload, 0, RPI_LENGTH);
        System.arraycopy(aem.getData(), 0, payload, RPI_LENGTH, AEM_LENGTH);
        return payload;
    }
}

