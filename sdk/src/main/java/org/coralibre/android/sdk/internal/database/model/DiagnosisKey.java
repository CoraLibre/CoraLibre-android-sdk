package org.coralibre.android.sdk.internal.database.model;

import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;

public class DiagnosisKey {

    byte[] keyData;

    ENInterval rollingStartNumber;

    ENInterval rollingPeriod;

    /**
     * A number in range [0, 7]
     * @see ExposureConfiguration#getTransmissionRiskScore(int)
     */
    int transmissionRiskLevel;

    public DiagnosisKey(byte[] keyData,
                        long rollingStartNumber,
                        long rollingPeriod,
                        int transmissionRiskLevel) {
        this.keyData = keyData;
        this.rollingStartNumber = new ENInterval(rollingStartNumber);
        this.rollingPeriod = new ENInterval(rollingPeriod);
        this.transmissionRiskLevel = transmissionRiskLevel;
    }

    public byte[] getKeyData() {
        return keyData;
    }

    public ENInterval getRollingStartNumber() {
        return rollingStartNumber;
    }

    public ENInterval getRollingPeriod() {
        return rollingPeriod;
    }

    public int getTransmissionRiskLevel() {
        return transmissionRiskLevel;
    }
}
