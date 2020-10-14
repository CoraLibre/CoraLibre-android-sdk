package org.coralibre.android.sdk.internal.datatypes;

import org.coralibre.android.sdk.internal.EnFrameworkConstants;

import java.security.InvalidParameterException;


public class BluetoothPayload {
    private final RollingProximityIdentifier rpi;
    private final AssociatedEncryptedMetadata aem;

    public BluetoothPayload(final byte[] rawPayload, final ENInterval interval) {
        if(rawPayload.length != EnFrameworkConstants.BLE_PAYLOAD_LENGTH)
            throw new InvalidParameterException("wrong payload size");
        byte[] rawRpi = new byte[EnFrameworkConstants.RPI_LENGTH];
        byte[] rawAem = new byte[EnFrameworkConstants.AEM_LENGTH];
        System.arraycopy(rawPayload, 0, rawRpi, 0, EnFrameworkConstants.RPI_LENGTH);
        System.arraycopy(rawPayload, EnFrameworkConstants.RPI_LENGTH, rawAem, 0, EnFrameworkConstants.AEM_LENGTH);
        this.rpi = new RollingProximityIdentifier(rawRpi, interval);
        this.aem = new AssociatedEncryptedMetadata(rawAem);
    }

    public BluetoothPayload(final RollingProximityIdentifier rpi, final AssociatedEncryptedMetadata aem) {
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
        byte[] payload = new byte[EnFrameworkConstants.BLE_PAYLOAD_LENGTH];
        System.arraycopy(rpi.getData(), 0, payload, 0, EnFrameworkConstants.RPI_LENGTH);
        System.arraycopy(aem.getData(), 0, payload, EnFrameworkConstants.RPI_LENGTH, EnFrameworkConstants.AEM_LENGTH);
        return payload;
    }
}

