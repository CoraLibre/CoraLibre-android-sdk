package org.coralibre.android.sdk.internal.matching.intermediateDatatypes

import org.coralibre.android.sdk.internal.datatypes.AssociatedMetadata
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifierKey

/**
 * A match is associated/build from exactly 1 bluetooth payload
 * Each match corresponds to a single "sighting".
 *
 * The term "sighting", which corresponds to one Match is used in:
 * https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java
 */
data class Match(
    val rpik: RollingProximityIdentifierKey,
    val metadata: AssociatedMetadata,
    val captureTimestampMillis: Long,
    val rssi: Byte
)
