package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import org.coralibre.android.sdk.internal.database.ppcp.model.CapturedData;

/**
 * Contains the parameters of the risk calculation algorithm. In particular:
 * {@code riskScore = attenuationScore * daysSinceLastExposureScore * durationScore * transmissionRiskScore}
 * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
 * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#exposureconfiguration">documentation on developers.google.com</a>
 */
public class ExposureConfiguration {

    //
    // The gms ExposureConfiguration is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //

    // using -1 values as default to indicate missing value for debugging purposes

    /**
     * The only fields affected by this value are {@link ExposureSummary#maximumRiskScore},
     * {@link ExposureSummary#summationRiskScore} and {@link ExposureInformation#totalRiskScore},
     * which ignore exposure incidents with lower scores. Default is no minimum (i.e. {@code 0}).
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#minimumriskscore">documentation on developers.google.com</a>
     */
    public int minimumRiskScore = 0;

    /**
     * Contains how much risk should be associated with every possible range of attenuation values
     * for an exposure. In particular (where A is the bluetooth attenuation value from an exposure:
     * A = {@code transmission power - } {@link CapturedData#getRssi()}):<br>
     * attenuationScores[0]  |  A > 73dB         <br>
     * attenuationScores[1]  |  73dB >= A > 63dB <br>
     * attenuationScores[2]  |  63dB >= A > 51dB <br>
     * attenuationScores[3]  |  51dB >= A > 33dB <br>
     * attenuationScores[4]  |  33dB >= A > 27dB <br>
     * attenuationScores[5]  |  27dB >= A > 15dB <br>
     * attenuationScores[6]  |  15dB >= A > 10dB <br>
     * attenuationScores[7]  |  A <= 10dB
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#attenuationscores">documentation on developers.google.com</a>
     */
    public int[] attenuationScores = null;

    /**
     * Contains how much risk should be associated with every possible day range since last
     * exposure. In particular:<br>
     * daysSinceLastExposureScores[0]  |  >= 14 days <br>
     * daysSinceLastExposureScores[1]  |  12-13 days <br>
     * daysSinceLastExposureScores[2]  |  10-11 days <br>
     * daysSinceLastExposureScores[3]  |  8-9 days   <br>
     * daysSinceLastExposureScores[4]  |  6-7 days   <br>
     * daysSinceLastExposureScores[5]  |  4-5 days   <br>
     * daysSinceLastExposureScores[6]  |  2-3 days   <br>
     * daysSinceLastExposureScores[7]  |  0-1 days
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#dayssincelastexposurescores">documentation on developers.google.com</a>
     */
    public int[] daysSinceLastExposureScores = null;

    /**
     * Contains how much risk should be associated with every possible time range of the duration
     * of an exposure. In particular (where D is the duration of an exposure):<br>
     * durationScores[0]  |  D = 0min           <br>
     * durationScores[1]  |  D <= 5min          <br>
     * durationScores[2]  |  5min < D <= 10min  <br>
     * durationScores[3]  |  10min < D <= 15min <br>
     * durationScores[4]  |  15min < D <= 20min <br>
     * durationScores[5]  |  20min < D <= 25min <br>
     * durationScores[6]  |  25min < D <= 30min <br>
     * durationScores[7]  |  D > 30min
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#durationscores">documentation on developers.google.com</a>
     */
    public int[] durationScores = null;

    /**
     * Contains how much transmission risk should be associated to an encounter. The risk factor
     * (i.e. the usage of these values) is app-defined, and for example a high risk could be
     * assigned to encounters with users who recently tested positive, and a lower score to
     * encounters with old positives
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#transmissionriskscores">documentation on developers.google.com</a>
     */
    public int[] transmissionRiskScores = null;

    /**
     * Contains the thresholds used to calculate the three attenuation duration buckets for
     * {@link ExposureSummary#attenuationDurations}. In particular:<br>
     * durationAtAttenuationThresholds[0]  |  low threshold (defaults to 50)  <br>
     * durationAtAttenuationThresholds[1]  |  high threshold (defaults to 70) <br>
     * <br>
     * These values are used to calculate the three attenuation duration ranges:<br>
     * 1. Attenuation <= durationAtAttenuationThresholds[0]                                      <br>
     * 2. durationAtAttenuationThresholds[0] < Attenuation <= durationAtAttenuationThresholds[1] <br>
     * 3. Y < durationAtAttenuationThresholds[1]
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration/3601128-attenuationdurationthresholds">documentation on developer.apple.com</a>
     */
    public int[] durationAtAttenuationThresholds = new int[] {50, 70};



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
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
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
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
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
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
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
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
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


        public ExposureConfigurationBuilder setMinimumRiskScore(int val) {
            builtObject.minimumRiskScore = val;
            return this;
        }


        public ExposureConfigurationBuilder setDurationAtAttenuationThresholds(
                int val0, int val1) {
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
