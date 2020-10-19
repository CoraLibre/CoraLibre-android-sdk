package org.coralibre.android.sdk.internal.matching.intermediateDatatypes;

import org.coralibre.android.sdk.internal.datatypes.AssociatedMetadata;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifierKey;

/**
 * A match is associated/build from exactly 1 bluetooth payload
 * Each match corresponds to a single "sighting".
 * */
public class Match {
    /*
     * The term "sighting", which corresponds to one Match is used in:
     * https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java
     */

    public final RollingProximityIdentifierKey rpik;
    public final AssociatedMetadata metadata;
    public final long captureTimestampMillis;
    public final byte rssi;

    public Match(
        final RollingProximityIdentifierKey rpik,
        final AssociatedMetadata metadata,
        final long captureTimestamp,
        final byte rssi
    ) {
        this.rpik = rpik;
        this.metadata = metadata;
        this.captureTimestampMillis = captureTimestamp;
        this.rssi = rssi;
    }

}
