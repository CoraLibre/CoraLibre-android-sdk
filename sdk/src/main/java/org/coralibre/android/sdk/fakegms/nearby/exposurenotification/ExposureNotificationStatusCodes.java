package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import org.coralibre.android.sdk.fakegms.common.api.CommonStatusCodes;

public final class ExposureNotificationStatusCodes extends CommonStatusCodes {

    private ExposureNotificationStatusCodes() {
    }

    //
    // The gms ExposureNotificationStatusCodes is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationPermissionHelper
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationPermissionHelper.kt
    //
    //
    public static final int FAILED = CommonStatusCodes.ERROR;
    public static final int FAILED_ALREADY_STARTED = 39500;
    public static final int FAILED_NOT_SUPPORTED = 39501;
    public static final int FAILED_REJECTED_OPT_IN = 39502;
    public static final int FAILED_SERVICE_DISABLED = 39503;
    public static final int FAILED_BLUETOOTH_DISABLED = 39504;
    public static final int FAILED_TEMPORARILY_DISABLED = 39505;
    public static final int FAILED_DISK_IO = 39506;
    public static final int FAILED_UNAUTHORIZED = 39507;
    public static final int FAILED_RATE_LIMITED = 39508;

    public static String getStatusCodeString(int statusCode) {
        switch (statusCode) {
            case 39500:
                return "FAILED_ALREADY_STARTED";
            case 39501:
                return "FAILED_NOT_SUPPORTED";
            case 39502:
                return "FAILED_REJECTED_OPT_IN";
            case 39503:
                return "FAILED_SERVICE_DISABLED";
            case 39504:
                return "FAILED_BLUETOOTH_DISABLED";
            case 39505:
                return "FAILED_TEMPORARILY_DISABLED";
            case 39506:
                return "FAILED_DISK_IO";
            case 39507:
                return "FAILED_UNAUTHORIZED";
            case 39508:
                return "FAILED_RATE_LIMITED";
            default:
                return CommonStatusCodes.getStatusCodeString(statusCode);
        }
    }
}
