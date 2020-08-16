package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import java.util.List;

public class ExposureInformation {

    //TODO Implement
    // See: src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt

    public List<Integer> attenuationDurationsInMinutes;
    public int attenuationValue;
    public int durationMinutes;
    public int totalRiskScore;
    public int transmissionRiskLevel;
    public int dateMillisSinceEpoch;

}
