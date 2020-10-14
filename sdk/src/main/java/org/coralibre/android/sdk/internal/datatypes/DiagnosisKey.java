package org.coralibre.android.sdk.internal.datatypes;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;

public class DiagnosisKey {

    private final TemporaryExposureKey_internal tek;

    /**
     * A number in range [0, 7]
     * @see ExposureConfiguration#getTransmissionRiskScore(int)
     */
    private final int transmissionRiskLevel;

    public DiagnosisKey(final TemporaryExposureKey_internal tek,
                        final int transmissionRiskLevel) {
        this.tek = tek;
        this.transmissionRiskLevel = transmissionRiskLevel;
    }

    public byte[] getKeyData() {
        return tek.getKey();
    }

    public ENInterval getInterval() {
        return tek.getInterval();
    }

    public int getTransmissionRiskLevel() {
        return transmissionRiskLevel;
    }
}
