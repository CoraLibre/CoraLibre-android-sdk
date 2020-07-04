package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

public class TemporaryExposureKey {

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

    public byte[] keyData;
    public int rollingStartIntervalNumber;
    public int rollingPeriod;
    public int transmissionRiskLevel;


    // TODO: Match with internal.crypto.ppcp.TemporaryExposureKey / create a converter


    public static class TemporaryExposureKeyBuilder {
        // This one imported and used inside the
        //  src/test/java/de/rki/coronawarnapp/transaction/SubmitDiagnosisKeysTransactionTest.kt

        private TemporaryExposureKey builtObject = new TemporaryExposureKey();
        public TemporaryExposureKeyBuilder() {}



        public TemporaryExposureKey.TemporaryExposureKeyBuilder setKeyData(
                byte[] data
        ) {
            builtObject.keyData = data;
            return this;
        }


        public TemporaryExposureKey.TemporaryExposureKeyBuilder setRollingPeriod(
                int val
        ) {
            builtObject.rollingPeriod = val;
            return this;
        }


        public TemporaryExposureKey.TemporaryExposureKeyBuilder setRollingStartIntervalNumber(
                int val
        ) {
            builtObject.rollingStartIntervalNumber = val;
            return this;
        }


        public TemporaryExposureKey.TemporaryExposureKeyBuilder setTransmissionRiskLevel(
                int val
        ) {
            builtObject.transmissionRiskLevel = val;
            return this;
        }


        public TemporaryExposureKey build() {
            return builtObject;
        }

    }


}
