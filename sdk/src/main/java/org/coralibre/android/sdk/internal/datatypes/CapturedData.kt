package org.coralibre.android.sdk.internal.datatypes

class CapturedData {
    /**
     * Milliseconds since Epoch.
     */
    val captureTimestampMillis: Long

    /**
     * the 10-minute interval
     */
    val enInterval: ENInterval

    /** in dB  */
    val rssi: Byte

    /** 16 bytes  */
    val rpi: RollingProximityIdentifier

    /** 4 bytes  */
    val aem: AssociatedEncryptedMetadata

    constructor(
        captureTimestamp: Long,
        rssi: Byte,
        rpi: RollingProximityIdentifier,
        aem: AssociatedEncryptedMetadata
    ) {
        captureTimestampMillis = captureTimestamp
        enInterval = ENInterval(captureTimestamp, true)
        this.rssi = rssi
        this.rpi = rpi
        this.aem = aem
    }

    constructor(
        captureTimestamp: Long,
        rssi: Int,
        rpi: RollingProximityIdentifier,
        aem: AssociatedEncryptedMetadata
    ) {
        captureTimestampMillis = captureTimestamp
        enInterval = ENInterval(captureTimestamp, true)
        this.rssi = rssi.toByte()
        this.rpi = rpi
        this.aem = aem
    }

    val rssiInt: Int
        get() = rssi.toInt()
}
