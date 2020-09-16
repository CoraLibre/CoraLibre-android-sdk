package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public final class ExposureSummary implements Parcelable {

    //
    // The gms ExposureSummary is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //

    // using -1 values as default to indicate missing value for debugging purposes


    private final int daysSinceLastExposure;
    private final int matchedKeyCount;
    private final int maximumRiskScore;
    private final int[] attenuationDurations;
    private final int summationRiskScore;

    public ExposureSummary(
        int daysSinceLastExposure,
        int matchedKeyCount,
        int maximumRiskScore,
        int[] attenuationDurations,
        int summationRiskScore
    ) {
        this.daysSinceLastExposure = daysSinceLastExposure;
        this.matchedKeyCount = matchedKeyCount;
        this.maximumRiskScore = maximumRiskScore;
        this.attenuationDurations = attenuationDurations;
        this.summationRiskScore = summationRiskScore;
    }

    // TODO Implement further
    //  For that see:
    //  src/main/java/de/rki/coronawarnapp/risk/RiskLevelCalculation.kt
    //  src/main/java/de/rki/coronawarnapp/storage/ExposureSummaryRepository.kt

    private ExposureSummary(Parcel in) {
        daysSinceLastExposure = in.readInt();
        matchedKeyCount = in.readInt();
        maximumRiskScore = in.readInt();
        attenuationDurations = in.createIntArray();
        summationRiskScore = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(daysSinceLastExposure);
        dest.writeInt(matchedKeyCount);
        dest.writeInt(maximumRiskScore);
        dest.writeIntArray(attenuationDurations);
        dest.writeInt(summationRiskScore);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExposureSummary> CREATOR = new Creator<ExposureSummary>() {
        @Override
        public ExposureSummary createFromParcel(Parcel in) {
            return new ExposureSummary(in);
        }

        @Override
        public ExposureSummary[] newArray(int size) {
            return new ExposureSummary[size];
        }
    };

    /**
     * The number of days since the last exposure (i.e. the last collected key resulting positive)
     *
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#dayssincelastexposure">documentation on developers.google.com</a>
     */
    public int getDaysSinceLastExposure() {
        return daysSinceLastExposure;
    }

    /**
     * The number of matched positive keys
     *
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#matchedkeycount">documentation on developers.google.com</a>
     */
    public int getMatchedKeyCount() {
        return matchedKeyCount;
    }

    /**
     * The maximum risk score obtained from all exposures
     *
     * @see <a href="https://github.com/corona-warn-app/cwa-app-android/blob/4906bd3b16959b1bca336c081a3718ac06f928c4/Corona-Warn-App/src/test/java/de/rki/coronawarnapp/storage/ExposureSummaryRepositoryTest.kt">used inside de/rki/coronawarnapp/storage/ExposureSummaryRepositoryTest.kt</a>
     * @see <a href="">documentation on developers.google.com</a>
     */
    public int getMaximumRiskScore() {
        return maximumRiskScore;
    }

    /**
     * Contains the cumulative duration in minutes the user spent in the three ranges of risk
     * defined by {@link ExposureConfiguration#durationAtAttenuationThresholds}. In particular:<br>
     * attenuationDurations[0]  |  sum of durations of exposures with an attenuation less than the
     * low threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[0])<br>
     * attenuationDurations[1]  |  sum of durations of exposures with an attenuation greater than
     * equal to the low threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[0])
     * but less than the high threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[1])<br>
     * attenuationDurations[2]  |  sum of durations of exposures with an attenuation greater than
     * or equal to the high threshold (i.e. {@link ExposureConfiguration#durationAtAttenuationThresholds}[1])<br>
     *
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#attenuationdurations">documentation on developers.google.com</a>
     */
    public int[] getAttenuationDurations() {
        return attenuationDurations;
    }

    /**
     * The sum of risk scores of all exposures
     *
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#summationriskscore">documentation on developers.google.com</a>
     */
    public int getSummationRiskScore() {
        return summationRiskScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureSummary that = (ExposureSummary) o;
        return daysSinceLastExposure == that.daysSinceLastExposure &&
            matchedKeyCount == that.matchedKeyCount &&
            maximumRiskScore == that.maximumRiskScore &&
            summationRiskScore == that.summationRiskScore &&
            attenuationDurations == that.attenuationDurations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            daysSinceLastExposure,
            matchedKeyCount,
            maximumRiskScore,
            summationRiskScore,
            attenuationDurations
        );
    }

    public static final class ExposureSummaryBuilder {
        // This one imported and used inside the
        //  src/test/java/de/rki/coronawarnapp/risk/RiskLevelCalculationTest.kt
        private int daysSinceLastExposure = 0;
        private int matchedKeyCount = 0;
        private int maximumRiskScore = 0;
        private int[] attenuationDurations = new int[]{0, 0, 0};
        private int summationRiskScore = 0;

        public ExposureSummaryBuilder() {
        }

        public ExposureSummaryBuilder setMaximumRiskScore(
            int maximumRiskScore
        ) {
            this.maximumRiskScore = maximumRiskScore;
            return this;
        }

        public ExposureSummaryBuilder setAttenuationDurations(
            int[] attenuationDurations
        ) {
            this.attenuationDurations = attenuationDurations;
            return this;
        }

        public ExposureSummaryBuilder setDaysSinceLastExposure(
            int daysSinceLastExposure
        ) {
            this.daysSinceLastExposure = daysSinceLastExposure;
            return this;
        }

        public ExposureSummaryBuilder setMatchedKeyCount(
            int matchedKeyCount
        ) {
            this.matchedKeyCount = matchedKeyCount;
            return this;
        }


        public ExposureSummaryBuilder setSummationRiskScore(
            int summationRiskScore
        ) {
            this.summationRiskScore = summationRiskScore;
            return this;
        }

        public ExposureSummary build() {
            return new ExposureSummary(
                daysSinceLastExposure,
                matchedKeyCount,
                maximumRiskScore,
                attenuationDurations,
                summationRiskScore
            );
        }
    }

}
