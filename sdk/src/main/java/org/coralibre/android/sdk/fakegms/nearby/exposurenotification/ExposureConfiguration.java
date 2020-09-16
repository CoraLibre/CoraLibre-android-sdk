package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import android.os.Parcelable;

import org.coralibre.android.sdk.internal.database.model.CapturedData;

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
    private final int minimumRiskScore;

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
    private final int[] attenuationScores;

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
    private final int[] daysSinceLastExposureScores;

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
    private final int[] durationScores;

    /**
     * Contains how much transmission risk should be associated to an encounter. The risk factor
     * (i.e. the usage of these values) is app-defined, and for example a high risk could be
     * assigned to encounters with users who recently tested positive, and a lower score to
     * encounters with old positives
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#transmissionriskscores">documentation on developers.google.com</a>
     */
    private final int[] transmissionRiskScores;

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
    private final int[] durationAtAttenuationThresholds;

    private ExposureConfiguration(final int minimumRiskScore,
                                  final int[] attenuationScores,
                                  final int[] daysSinceLastExposureScores,
                                  final int[] durationScores,
                                  final int[] transmissionRiskScores,
                                  final int[] durationAtAttenuationThresholds) {

        this.minimumRiskScore = minimumRiskScore;
        this.attenuationScores = attenuationScores;
        this.daysSinceLastExposureScores = daysSinceLastExposureScores;
        this.durationScores = durationScores;
        this.transmissionRiskScores = transmissionRiskScores;
        this.durationAtAttenuationThresholds = durationAtAttenuationThresholds;
    }


    /**
     * @see #minimumRiskScore
     */
    public int getMinimumRiskScore() {
        return minimumRiskScore;
    }

    /**
     * @see #durationAtAttenuationThresholds
     */
    public int getDurationAtAttenuationLowThreshold() {
        return durationAtAttenuationThresholds[0];
    }

    /**
     * @see #durationAtAttenuationThresholds
     */
    public int getDurationAtAttenuationHighThreshold() {
        return durationAtAttenuationThresholds[1];
    }


    /**
     * @param attenuationValue the bluetooth attenuation value from an exposure, in dB:
     *                         {@code transmission power - } {@link CapturedData#getRssi()}
     * @return the attenuation score for the provided value
     * @see #attenuationScores
     */
    public int getAttenuationScore(final int attenuationValue) {
        if (attenuationValue > 73) {
            return attenuationScores[0];
        } else if (attenuationValue > 63) {
            return attenuationScores[1];
        } else if (attenuationValue > 51) {
            return attenuationScores[2];
        } else if (attenuationValue > 33) {
            return attenuationScores[3];
        } else if (attenuationValue > 27) {
            return attenuationScores[4];
        } else if (attenuationValue > 15) {
            return attenuationScores[5];
        } else if (attenuationValue > 10) {
            return attenuationScores[6];
        } else {
            return attenuationScores[7];
        }
    }

    /**
     * @param daysSinceLastExposureValue the number of days since the last exposure
     * @return the days since last exposure score for the provided value
     * @see #daysSinceLastExposureScores
     */
    public int getDaysSinceLastExposureScore(final int daysSinceLastExposureValue) {
        if (daysSinceLastExposureValue >= 14) {
            return daysSinceLastExposureScores[0];
        } else if (daysSinceLastExposureValue == 12 || daysSinceLastExposureValue == 13) {
            return daysSinceLastExposureScores[1];
        } else if (daysSinceLastExposureValue == 10 || daysSinceLastExposureValue == 11) {
            return daysSinceLastExposureScores[2];
        } else if (daysSinceLastExposureValue == 8 || daysSinceLastExposureValue == 9) {
            return daysSinceLastExposureScores[3];
        } else if (daysSinceLastExposureValue == 6 || daysSinceLastExposureValue == 7) {
            return daysSinceLastExposureScores[4];
        } else if (daysSinceLastExposureValue == 4 || daysSinceLastExposureValue == 5) {
            return daysSinceLastExposureScores[5];
        } else if (daysSinceLastExposureValue == 2 || daysSinceLastExposureValue == 3) {
            return daysSinceLastExposureScores[6];
        } else {
            return daysSinceLastExposureScores[7];
        }
    }

    /**
     * @param durationValue the duration of an exposure, in minutes
     * @return the duration score for the provided value
     * @see #durationScores
     */
    public int getDurationScore(final int durationValue) {
        if (durationValue <= 0) { // using <= instead of == just to be sure
            return durationScores[0];
        } else if (durationValue <= 5) {
            return durationScores[1];
        } else if (durationValue <= 10) {
            return durationScores[2];
        } else if (durationValue <= 15) {
            return durationScores[3];
        } else if (durationValue <= 20) {
            return durationScores[4];
        } else if (durationValue <= 25) {
            return durationScores[5];
        } else if (durationValue <= 30) {
            return durationScores[6];
        } else {
            return durationScores[7];
        }
    }

    /**
     * @param transmissionRiskValue the user defined risk value associated to an exposure,
     *                              must be >= 0 and <= 7
     * @return the transmission risk score for the provided value
     * @see #transmissionRiskScores
     */
    public int getTransmissionRiskScore(final int transmissionRiskValue) {
        return transmissionRiskScores[transmissionRiskValue];
    }


    /**
     * @param attenuationValue the bluetooth attenuation value from an exposure, in dB:
     *                         {@code transmission power - } {@link CapturedData#getRssi()}
     * @param daysSinceLastExposureValue the number of days since the last exposure
     * @param durationValue the duration of an exposure, in minutes
     * @param transmissionRiskValue the user defined risk value associated to an exposure,
     *                              must be >= 0 and <= 7
     * @return the final risk score calculated by multiplying together the intermediary risk scores
     * associated with the provided values
     * @see <a href="https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration">risk calculation algorithm details on developer.apple.com</a>
     */
    public int getRiskScore(final int attenuationValue,
                            final int daysSinceLastExposureValue,
                            final int durationValue,
                            final int transmissionRiskValue) {
        return getAttenuationScore(attenuationValue)
                * getDaysSinceLastExposureScore(daysSinceLastExposureValue)
                * getDurationScore(durationValue)
                * getTransmissionRiskScore(transmissionRiskValue);
    }


    public static class ExposureConfigurationBuilder {
        // This one imported and used inside the
        //  InternalExposureNotificationClient
        //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
        // and
        //  ApplicationConfigurationService
        //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/service/applicationconfiguration/ApplicationConfigurationService.kt


        private int minimumRiskScore = 0;
        private int[] attenuationScores = null;
        private int[] daysSinceLastExposureScores = null;
        private int[] durationScores = null;
        private int[] transmissionRiskScores = null;
        private final int[] durationAtAttenuationThresholds = new int[] {50, 70};

        public ExposureConfigurationBuilder() {}


        /**
         * if not called defaults to 0
         * @see ExposureConfiguration#minimumRiskScore
         * @return {@code this}
         */
        public ExposureConfigurationBuilder setMinimumRiskScore(int val) {
            minimumRiskScore = val;
            return this;
        }

        /**
         * @see ExposureConfiguration#attenuationScores
         * @return {@code this}
         */
        public ExposureConfigurationBuilder setAttenuationScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
            attenuationScores = new int[8];
            attenuationScores[0] = val0;
            attenuationScores[1] = val1;
            attenuationScores[2] = val2;
            attenuationScores[3] = val3;
            attenuationScores[4] = val4;
            attenuationScores[5] = val5;
            attenuationScores[6] = val6;
            attenuationScores[7] = val7;
            return this;
        }

        /**
         * @see ExposureConfiguration#daysSinceLastExposureScores
         * @return {@code this}
         */
        public ExposureConfigurationBuilder setDaysSinceLastExposureScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
            daysSinceLastExposureScores = new int[8];
            daysSinceLastExposureScores[0] = val0;
            daysSinceLastExposureScores[1] = val1;
            daysSinceLastExposureScores[2] = val2;
            daysSinceLastExposureScores[3] = val3;
            daysSinceLastExposureScores[4] = val4;
            daysSinceLastExposureScores[5] = val5;
            daysSinceLastExposureScores[6] = val6;
            daysSinceLastExposureScores[7] = val7;
            return this;
        }

        /**
         * @see ExposureConfiguration#durationScores
         * @return {@code this}
         */
        public ExposureConfigurationBuilder setDurationScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
            durationScores = new int[8];
            durationScores[0] = val0;
            durationScores[1] = val1;
            durationScores[2] = val2;
            durationScores[3] = val3;
            durationScores[4] = val4;
            durationScores[5] = val5;
            durationScores[6] = val6;
            durationScores[7] = val7;
            return this;
        }

        /**
         * @see ExposureConfiguration#transmissionRiskScores
         * @return {@code this}
         */
        public ExposureConfigurationBuilder setTransmissionRiskScores(
                int val0, int val1, int val2, int val3, int val4, int val5, int val6, int val7) {
            transmissionRiskScores = new int[8];
            transmissionRiskScores[0] = val0;
            transmissionRiskScores[1] = val1;
            transmissionRiskScores[2] = val2;
            transmissionRiskScores[3] = val3;
            transmissionRiskScores[4] = val4;
            transmissionRiskScores[5] = val5;
            transmissionRiskScores[6] = val6;
            transmissionRiskScores[7] = val7;
            return this;
        }

        /**
         * if not called defaults to {50, 70}
         * @see ExposureConfiguration#durationAtAttenuationThresholds
         * @return {@code this}
         */
        public ExposureConfigurationBuilder setDurationAtAttenuationThresholds(
                int lowThreshold, int highThreshold) {
            durationAtAttenuationThresholds[0] = lowThreshold;
            durationAtAttenuationThresholds[1] = highThreshold;
            return this;
        }


        /**
         * @return an {@link ExposureConfiguration} instance based on the values set in the builder
         */
        public ExposureConfiguration build() {
            return new ExposureConfiguration(minimumRiskScore,attenuationScores,
                    daysSinceLastExposureScores, durationScores, transmissionRiskScores,
                    durationAtAttenuationThresholds);
        }
    }
}
