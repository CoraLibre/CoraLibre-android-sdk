package org.coralibre.android.sdk.fakegms.common.api;

public class ApiException extends Exception {

    //
    // The gms ApiException is used in particular by the following classes (and perhaps more):
    //
    //  ExposureStateUpdateWorker
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt
    //
    //  InternalExposureNotificationPermissionHelper
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationPermissionHelper.kt
    //
    //



    public ApiException(String msg) {
        super(msg);
    }

    public ApiException(Status status) {
        super(statusToMsg(status));
    }


    private static String statusToMsg(Status status) {
        switch (status) {
            case RESULT_CANCELED:
                return "ApiException: RESULT_CANCELED";
        }
        return "ApiException: unknown";
    }


}
