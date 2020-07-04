package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

public class ExposureConfiguration {

    //
    // The gms ExposureConfiguration is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //

    public int minimumRiskScore = -1;
    public int[] attenuationScores = null;
    public int[] daysSinceLastExposureScores = null;
    public int[] durationScores = null;
    public int[] transmissionRiskScores = null;
    public int[] durationAtAttenuationThresholds = null;



    public static class ExposureConfigurationBuilder {
        // This one imported and used inside the
        //  InternalExposureNotificationClient
        //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
        // and
        //  ApplicationConfigurationService
        //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/service/applicationconfiguration/ApplicationConfigurationService.kt

        private ExposureConfiguration builtObject = new ExposureConfiguration();
        public ExposureConfigurationBuilder() {}


        public ExposureConfigurationBuilder setTransmissionRiskScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7
        ) {
            builtObject.transmissionRiskScores = new int[8];
            builtObject.transmissionRiskScores[0] = val0;
            builtObject.transmissionRiskScores[1] = val1;
            builtObject.transmissionRiskScores[2] = val2;
            builtObject.transmissionRiskScores[3] = val3;
            builtObject.transmissionRiskScores[4] = val4;
            builtObject.transmissionRiskScores[5] = val5;
            builtObject.transmissionRiskScores[6] = val6;
            builtObject.transmissionRiskScores[7] = val7;
            return this;
        }


        public ExposureConfigurationBuilder setDurationScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7
        ) {
            builtObject.durationScores = new int[8];
            builtObject.durationScores[0] = val0;
            builtObject.durationScores[1] = val1;
            builtObject.durationScores[2] = val2;
            builtObject.durationScores[3] = val3;
            builtObject.durationScores[4] = val4;
            builtObject.durationScores[5] = val5;
            builtObject.durationScores[6] = val6;
            builtObject.durationScores[7] = val7;
            return this;
        }


        public ExposureConfigurationBuilder setDaysSinceLastExposureScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7
        ) {
            builtObject.daysSinceLastExposureScores = new int[8];
            builtObject.daysSinceLastExposureScores[0] = val0;
            builtObject.daysSinceLastExposureScores[1] = val1;
            builtObject.daysSinceLastExposureScores[2] = val2;
            builtObject.daysSinceLastExposureScores[3] = val3;
            builtObject.daysSinceLastExposureScores[4] = val4;
            builtObject.daysSinceLastExposureScores[5] = val5;
            builtObject.daysSinceLastExposureScores[6] = val6;
            builtObject.daysSinceLastExposureScores[7] = val7;
            return this;
        }


        public ExposureConfigurationBuilder setAttenuationScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7
        ) {
            builtObject.attenuationScores = new int[8];
            builtObject.attenuationScores[0] = val0;
            builtObject.attenuationScores[1] = val1;
            builtObject.attenuationScores[2] = val2;
            builtObject.attenuationScores[3] = val3;
            builtObject.attenuationScores[4] = val4;
            builtObject.attenuationScores[5] = val5;
            builtObject.attenuationScores[6] = val6;
            builtObject.attenuationScores[7] = val7;
            return this;
        }


        public ExposureConfigurationBuilder setMinimumRiskScore(
                int val
        ) {
            builtObject.minimumRiskScore = val;
            return this;
        }


        public ExposureConfigurationBuilder setDurationAtAttenuationThresholds(
                int val0, int val1
        ) {
            builtObject.durationAtAttenuationThresholds = new int[2];
            builtObject.durationAtAttenuationThresholds[0] = val0;
            builtObject.durationAtAttenuationThresholds[1] = val1;
            return this;
        }


        public ExposureConfiguration build() {
            return builtObject;
        }

    }


}
