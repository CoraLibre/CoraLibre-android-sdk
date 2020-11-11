package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RiskScoreAlgorithmTest {

    @Test
    public void appleExample() {
        // taken from https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration
        final ExposureConfiguration exposureConfiguration =
                new ExposureConfiguration.ExposureConfigurationBuilder()
                        .setTransmissionRiskScores(     0, 0, 0, 0, 0, 7, 0, 0)
                        .setDurationScores(             1, 1, 4, 7, 7, 8, 8, 8)
                        .setDaysSinceLastExposureScores(1, 2, 2, 4, 6, 8, 8, 8)
                        .setAttenuationScores(          1, 1, 1, 8, 8, 8, 8, 8)
                        .build();

        assertEquals(392, exposureConfiguration.getRiskScore(68, 4, 14, 5));
    }
}
