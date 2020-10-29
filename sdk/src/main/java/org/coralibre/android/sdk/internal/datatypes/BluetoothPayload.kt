package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class BluetoothPayload {
    val rpi: RollingProximityIdentifier
    val aem: AssociatedEncryptedMetadata

    constructor(rawPayload: ByteArray, interval: ENInterval) {
        if (rawPayload.size != EnFrameworkConstants.BLE_PAYLOAD_LENGTH) {
            throw InvalidParameterException("wrong payload size")
        }
        val rawRpi = ByteArray(EnFrameworkConstants.RPI_LENGTH)
        System.arraycopy(rawPayload, 0, rawRpi, 0, EnFrameworkConstants.RPI_LENGTH)

        val rawAem = ByteArray(EnFrameworkConstants.AEM_LENGTH)
        System.arraycopy(
            rawPayload,
            EnFrameworkConstants.RPI_LENGTH,
            rawAem,
            0,
            EnFrameworkConstants.AEM_LENGTH
        )
        rpi = RollingProximityIdentifier(rawRpi, interval)
        aem = AssociatedEncryptedMetadata(rawAem)
    }

    constructor(rpi: RollingProximityIdentifier, aem: AssociatedEncryptedMetadata) {
        this.rpi = rpi
        this.aem = aem
    }

    val interval: ENInterval
        get() = rpi.interval
    val rawPayload: ByteArray
        get() {
            val payload = ByteArray(EnFrameworkConstants.BLE_PAYLOAD_LENGTH)
            System.arraycopy(rpi.getData(), 0, payload, 0, EnFrameworkConstants.RPI_LENGTH)
            System.arraycopy(
                aem.data,
                0,
                payload,
                EnFrameworkConstants.RPI_LENGTH,
                EnFrameworkConstants.AEM_LENGTH
            )
            return payload
        }
}
