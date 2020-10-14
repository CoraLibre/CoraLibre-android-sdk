package org.coralibre.android.sdk.internal.datatypes;


public class CapturedData {


    /**
     *  Milliseconds since Epoch.
     */
    @Deprecated
    private final long captureTimestamp;

    /**
     * the 10-minute interval
     */
    private final ENInterval enInterval;

    /** in dB */
    private final byte rssi;

    /** 16 bytes */
    private final RollingProximityIdentifier rpi;

    /** 4 bytes */
    private final AssociatedEncryptedMetadata aem;


    public CapturedData(
        final long captureTimestamp,
        final byte rssi,
        final RollingProximityIdentifier rpi,
        final AssociatedEncryptedMetadata aem
    ) {
        this.captureTimestamp = captureTimestamp;
        this.enInterval = new ENInterval(captureTimestamp, true);
        this.rssi = rssi;
        this.rpi = rpi;
        this.aem = aem;
    }

    public Long getCaptureTimestamp() {
        return captureTimestamp;
    }

    public ENInterval getEnInterval() {
        return enInterval;
    }

    public byte getRssi() {
        return rssi;
    }

    public RollingProximityIdentifier getRpi() {
        return rpi;
    }

    public AssociatedEncryptedMetadata getAem() {
        return aem;
    }
}
