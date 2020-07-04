package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;


public class ExposureSummary {

    //
    // The gms ExposureSummary is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //

    // using -1 values as default to indicate missing value for debugging purposes

    /**
     * The number of days since the last exposure (i.e. the last collected key resulting positive)
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#dayssincelastexposure">documentation on developers.google.com</a>
     */
    public int daysSinceLastExposure = -1;

    /**
     * The number of matched positive keys
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#matchedkeycount">documentation on developers.google.com</a>
     */
    public int matchedKeyCount = -1;

    /**
     * The maximum risk score obained from all exposures
     * @see <a href="https://github.com/corona-warn-app/cwa-app-android/blob/4906bd3b16959b1bca336c081a3718ac06f928c4/Corona-Warn-App/src/test/java/de/rki/coronawarnapp/storage/ExposureSummaryRepositoryTest.kt">used inside de/rki/coronawarnapp/storage/ExposureSummaryRepositoryTest.kt</a>
     * @see <a href="">documentation on developers.google.com</a>
     */
    public int maximumRiskScore = -1;

    /**
     * Contains the cumulative duration in minutes the user spent in the three ranges of risk
     * defined by {@link ExposureConfiguration#durationAtAttenuationThresholds}. In particular:<br>
     * attenuationDurations[0]  |  sum of durations of exposures with an attenuation less than the
     *                             low threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[0])<br>
     * attenuationDurations[1]  |  sum of durations of exposures with an attenuation greater than
     *                             equal to the low threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[0])
     *                             but less than the high threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[1])<br>
     * attenuationDurations[2]  |  sum of durations of exposures with an attenuation greater than
     *                             or equal to the high threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[1])<br>
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#attenuationdurations">documentation on developers.google.com</a>
     */
    public int[] attenuationDurations = null;

    /**
     * The sum of risk scores of all exposures
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#summationriskscore">documentation on developers.google.com</a>
     */
    public int summationRiskScore = -1;

    // TODO Implement further
    //  For that see:
    //  src/main/java/de/rki/coronawarnapp/risk/RiskLevelCalculation.kt
    //  src/main/java/de/rki/coronawarnapp/storage/ExposureSummaryRepository.kt


    public static class ExposureSummaryBuilder {
        // This one imported and used inside the
        //  src/test/java/de/rki/coronawarnapp/risk/RiskLevelCalculationTest.kt

        private ExposureSummary builtObject = new ExposureSummary();
        public ExposureSummaryBuilder() {}



        public ExposureSummary.ExposureSummaryBuilder setMaximumRiskScore(
                int val
        ) {
            builtObject.maximumRiskScore = val;
            return this;
        }


        public ExposureSummary.ExposureSummaryBuilder setAttenuationDurations(
                int[] vals
        ) {
            builtObject.attenuationDurations = vals;
            return this;
        }


        public ExposureSummary.ExposureSummaryBuilder setDaysSinceLastExposure(
                int val
        ) {
            builtObject.daysSinceLastExposure = val;
            return this;
        }


        public ExposureSummary.ExposureSummaryBuilder setMatchedKeyCount(
                int val
        ) {
            builtObject.matchedKeyCount = val;
            return this;
        }


        public ExposureSummary.ExposureSummaryBuilder setSummationRiskScore(
                int val
        ) {
            builtObject.summationRiskScore = val;
            return this;
        }


        public ExposureSummary build() {
            return builtObject;
        }

    }

}
