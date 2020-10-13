package org.coralibre.android.sdk.internal.database.model;

import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;

public class DiagnosisKey {

    byte[] keyData;

    private final ENInterval interval;

    /**
     * A number in range [0, 7]
     * @see ExposureConfiguration#getTransmissionRiskScore(int)
     */
    int transmissionRiskLevel;

    public DiagnosisKey(byte[] keyData,
                        long intervalNumber,
                        int transmissionRiskLevel) {
        this.keyData = keyData;
        this.interval = new ENInterval(intervalNumber);
        this.transmissionRiskLevel = transmissionRiskLevel;
    }

    public byte[] getKeyData() {
        return keyData;
    }

    public ENInterval getInterval() {
        return interval;
    }

    public int getTransmissionRiskLevel() {
        return transmissionRiskLevel;
    }
}
