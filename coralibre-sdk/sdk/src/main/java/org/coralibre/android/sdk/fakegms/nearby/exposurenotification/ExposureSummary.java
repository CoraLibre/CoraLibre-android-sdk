package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;


public class ExposureSummary {

    //
    // The gms ExposureSummary is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //


    // Fill with -1 to make debugging easier:
    public int daysSinceLastExposure = -1;
    public int matchedKeyCount = -1;

    public int summationRiskScore = -1;
        // this value is not documented in the API docs, but used inside:
        // de/rki/coronawarnapp/storage/ExposureSummaryRepositoryTest.kt

    public int maximumRiskScore = -1;
    public int[] attenuationDurationsInMinutes = null;

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
            builtObject.attenuationDurationsInMinutes = vals;
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
