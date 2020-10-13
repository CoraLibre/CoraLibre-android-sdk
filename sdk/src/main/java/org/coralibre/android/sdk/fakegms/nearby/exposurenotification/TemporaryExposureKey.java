package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public final class TemporaryExposureKey implements Parcelable {

    //
    // The gms TemporaryExposureKey is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationPermissionHelper
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationPermissionHelper.kt
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //
    //

    private final byte[] keyData;
    private final int rollingStartIntervalNumber;
    private final int transmissionRiskLevel;
    private final int rollingPeriod;
    private final int reportType;

    public TemporaryExposureKey(
        byte[] keyData,
        int rollingStartIntervalNumber,
        int rollingPeriod,
        int transmissionRiskLevel,
        int reportType
    ) {
        this.keyData = keyData;
        this.rollingStartIntervalNumber = rollingStartIntervalNumber;
        this.rollingPeriod = rollingPeriod;
        this.transmissionRiskLevel = transmissionRiskLevel;
        this.reportType = reportType;
    }

    private TemporaryExposureKey(Parcel in) {
        keyData = in.createByteArray();
        rollingStartIntervalNumber = in.readInt();
        transmissionRiskLevel = in.readInt();
        rollingPeriod = in.readInt();
        reportType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(keyData);
        dest.writeInt(rollingStartIntervalNumber);
        dest.writeInt(transmissionRiskLevel);
        dest.writeInt(rollingPeriod);
        dest.writeInt(reportType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TemporaryExposureKey> CREATOR = new Creator<TemporaryExposureKey>() {
        @Override
        public TemporaryExposureKey createFromParcel(Parcel in) {
            return new TemporaryExposureKey(in);
        }

        @Override
        public TemporaryExposureKey[] newArray(int size) {
            return new TemporaryExposureKey[size];
        }
    };

    public byte[] getKeyData() {
        return keyData;
    }

    public int getRollingStartIntervalNumber() {
        return rollingStartIntervalNumber;
    }

    public int getTransmissionRiskLevel() {
        return transmissionRiskLevel;
    }

    public int getRollingPeriod() {
        return rollingPeriod;
    }

    public int getReportType() {
        return reportType;
    }

    // TODO: create a converter for internal.crypto.ppcp.TemporaryExposureKey_internal

    public static final class TemporaryExposureKeyBuilder {
        // This one imported and used inside the
        //  src/test/java/de/rki/coronawarnapp/transaction/SubmitDiagnosisKeysTransactionTest.kt

        private byte[] keyData;
        private int rollingStartIntervalNumber;
        private int transmissionRiskLevel;
        private int rollingPeriod;
        private int reportType;

        public TemporaryExposureKeyBuilder() {
        }

        public TemporaryExposureKeyBuilder setKeyData(byte[] keyData) {
            this.keyData = Arrays.copyOf(keyData, keyData.length);
            return this;
        }

        public TemporaryExposureKeyBuilder setRollingPeriod(int rollingPeriod) {
            this.rollingPeriod = rollingPeriod;
            return this;
        }

        public TemporaryExposureKeyBuilder setRollingStartIntervalNumber(
            int rollingStartIntervalNumber
        ) {
            this.rollingStartIntervalNumber = rollingStartIntervalNumber;
            return this;
        }

        public TemporaryExposureKeyBuilder setTransmissionRiskLevel(int transmissionRiskLevel) {
            this.transmissionRiskLevel = transmissionRiskLevel;
            return this;
        }

        public TemporaryExposureKeyBuilder setReportType(int reportType) {
            this.reportType = reportType;
            return this;
        }

        public TemporaryExposureKey build() {
            return new TemporaryExposureKey(
                keyData,
                rollingStartIntervalNumber,
                rollingPeriod,
                transmissionRiskLevel,
                reportType
            );
        }
    }
}
