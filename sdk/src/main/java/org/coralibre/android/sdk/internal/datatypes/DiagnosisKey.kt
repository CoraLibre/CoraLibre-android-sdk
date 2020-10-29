package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration

class DiagnosisKey(
    private val tek: InternalTemporaryExposureKey,
    /**
     * A number in range [0, 7]
     * @see ExposureConfiguration.getTransmissionRiskScore
     */
    val transmissionRiskLevel: Int
) {
    val keyData: ByteArray
        get() = tek.key
    val interval: ENInterval
        get() = tek.interval
}
