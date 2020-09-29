package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;

public final class ExposureInformation implements Parcelable {
    // See: src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt

    private final long dateMillisSinceEpoch;
    private final int durationMinutes;
    private final int attenuationValue;
    private final int transmissionRiskLevel;
    private final int totalRiskScore;
    @NonNull
    private final int[] attenuationDurationsInMinutes;

    private ExposureInformation(
        long dateMillisSinceEpoch,
        int durationMinutes,
        int attenuationValue,
        int transmissionRiskLevel,
        int totalRiskScore,
        @NonNull int[] attenuationDurationsInMinutes
    ) {
        this.dateMillisSinceEpoch = dateMillisSinceEpoch;
        this.durationMinutes = durationMinutes;
        this.attenuationValue = attenuationValue;
        this.transmissionRiskLevel = transmissionRiskLevel;
        this.totalRiskScore = totalRiskScore;
        this.attenuationDurationsInMinutes = attenuationDurationsInMinutes;
    }

    private ExposureInformation(Parcel in) {
        dateMillisSinceEpoch = in.readLong();
        durationMinutes = in.readInt();
        attenuationValue = in.readInt();
        transmissionRiskLevel = in.readInt();
        totalRiskScore = in.readInt();
        attenuationDurationsInMinutes = Objects.requireNonNull(in.createIntArray());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dateMillisSinceEpoch);
        dest.writeInt(durationMinutes);
        dest.writeInt(attenuationValue);
        dest.writeInt(transmissionRiskLevel);
        dest.writeInt(totalRiskScore);
        dest.writeIntArray(attenuationDurationsInMinutes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExposureInformation> CREATOR = new Creator<ExposureInformation>() {
        @Override
        public ExposureInformation createFromParcel(Parcel in) {
            return new ExposureInformation(in);
        }

        @Override
        public ExposureInformation[] newArray(int size) {
            return new ExposureInformation[size];
        }
    };

    public long getDateMillisSinceEpoch() {
        return dateMillisSinceEpoch;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getAttenuationValue() {
        return attenuationValue;
    }

    public int getTransmissionRiskLevel() {
        return transmissionRiskLevel;
    }

    public int getTotalRiskScore() {
        return totalRiskScore;
    }

    @NonNull
    public int[] getAttenuationDurationsInMinutes() {
        return Arrays.copyOf(attenuationDurationsInMinutes, attenuationDurationsInMinutes.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureInformation that = (ExposureInformation) o;
        return dateMillisSinceEpoch == that.dateMillisSinceEpoch &&
            durationMinutes == that.durationMinutes &&
            attenuationValue == that.attenuationValue &&
            transmissionRiskLevel == that.transmissionRiskLevel &&
            totalRiskScore == that.totalRiskScore &&
            attenuationDurationsInMinutes == that.attenuationDurationsInMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            dateMillisSinceEpoch,
            durationMinutes,
            attenuationValue,
            transmissionRiskLevel,
            totalRiskScore,
            attenuationDurationsInMinutes
        );
    }

    public static final class ExposureInformationBuilder {
        private long dateMillisSinceEpoch;
        private int durationMinutes;
        private int attenuationValue;
        private int transmissionRiskLevel;
        private int totalRiskScore;
        @NonNull
        private int[] attenuationDurationsInMinutes = new int[]{0, 0};

        public ExposureInformationBuilder setDateMillisSinceEpoch(long dateMillisSinceEpoch) {
            this.dateMillisSinceEpoch = dateMillisSinceEpoch;
            return this;
        }

        public ExposureInformationBuilder setDurationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public ExposureInformationBuilder setAttenuationValue(int attenuationValue) {
            this.attenuationValue = attenuationValue;
            return this;
        }

        public ExposureInformationBuilder setTransmissionRiskLevel(int transmissionRiskLevel) {
            this.transmissionRiskLevel = transmissionRiskLevel;
            return this;
        }

        public ExposureInformationBuilder setTotalRiskScore(int totalRiskScore) {
            this.totalRiskScore = totalRiskScore;
            return this;
        }

        public ExposureInformationBuilder setAttenuationDurations(
            @NonNull int[] attenuationDurationsInMinutes
        ) {
            this.attenuationDurationsInMinutes = Arrays.copyOf(
                attenuationDurationsInMinutes, attenuationDurationsInMinutes.length
            );
            return this;
        }

        public ExposureInformation build() {
            return new ExposureInformation(
                dateMillisSinceEpoch,
                durationMinutes,
                attenuationValue,
                transmissionRiskLevel,
                totalRiskScore,
                attenuationDurationsInMinutes
            );
        }
    }
}
