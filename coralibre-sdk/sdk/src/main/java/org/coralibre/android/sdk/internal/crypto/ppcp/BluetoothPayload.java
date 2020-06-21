package org.coralibre.android.sdk.internal.crypto.ppcp;
import java.security.InvalidParameterException;

import static org.coralibre.android.sdk.internal.crypto.ppcp.AssociatedMetadata.AEM_LENGTH;
import static org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier.RPI_SIZE;

public class BluetoothPayload {
    private RollingProximityIdentifier rpi;
    private AssociatedEncryptedMetadata aem;

    public BluetoothPayload(byte[] rawPayload, ENNumber interval) {
        if(rawPayload.length != RPI_SIZE + AEM_LENGTH)
            throw new InvalidParameterException("wrong payload size");
        byte[] rawRpi = new byte[RPI_SIZE];
        byte[] rawAem = new byte[AEM_LENGTH];
        System.arraycopy(rawPayload, 0, rawRpi, 0, RPI_SIZE);
        System.arraycopy(rawPayload, RPI_SIZE, rawAem, 0, AEM_LENGTH);
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

    public ENNumber getInterval() {
        return rpi.getInterval();
    }

    public byte[] getRawPayload() {
        byte[] payload = new byte[RPI_SIZE + AEM_LENGTH];
        System.arraycopy(rpi.getData(), 0, payload, 0, RPI_SIZE);
        System.arraycopy(aem.getData(), 0, payload, RPI_SIZE, AEM_LENGTH);
        return payload;
    }
}

