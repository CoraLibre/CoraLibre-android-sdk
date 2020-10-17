package org.coralibre.android.sdk.internal.datatypes;


public class CapturedData {


    /**
     *  Milliseconds since Epoch.
     */
    private final long captureTimestampMillis;

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
        this.captureTimestampMillis = captureTimestamp;
        this.enInterval = new ENInterval(captureTimestamp, true);
        this.rssi = rssi;
        this.rpi = rpi;
        this.aem = aem;
    }

    public Long getCaptureTimestampMillis() {
        return captureTimestampMillis;
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
