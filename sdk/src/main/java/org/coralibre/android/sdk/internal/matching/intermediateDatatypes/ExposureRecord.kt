package org.coralibre.android.sdk.internal.matching.intermediateDatatypes

/**
 * @param millisSinceEpoch The day that the interaction occurred.
 * @param attenuationValue The time-weighted average of the attenuation.
 * @param transmissionRiskLevel A transmission risk value.
 */
data class ExposureRecord(
    val millisSinceEpoch: Long,
    val durationSeconds: Long,
    val attenuationValue: Int,
    val transmissionRiskLevel: Int,
    val daysSinceExposure: Int
)
