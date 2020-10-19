package org.coralibre.android.sdk.internal.matching.intermediateDatatypes;

public class ExposureRecord {

    public final long millisSinceEpoch; // The day that the interaction occurred.
    public final long durationSeconds;
    public final int attenuationValue; // The time-weighted average of the attenuation.
    public final int transmissionRiskLevel; // A transmission risk value.
    public final int daysSinceExposure; // A transmission risk value.

    public ExposureRecord(
        final long millisSinceEpoch,
        final long durationSeconds,
        final int attenuationValue,
        final int transmissionRiskLevel,
        final int daysSinceExposure
    ) {
        this.millisSinceEpoch = millisSinceEpoch;
        this.durationSeconds = durationSeconds;
        this.attenuationValue = attenuationValue;
        this.transmissionRiskLevel = transmissionRiskLevel;
        this.daysSinceExposure = daysSinceExposure;
    }

}
