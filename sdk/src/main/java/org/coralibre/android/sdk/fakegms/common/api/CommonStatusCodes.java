package org.coralibre.android.sdk.fakegms.common.api;

public class CommonStatusCodes {
    public static final int SUCCESS = 0;
    public static final int RESOLUTION_REQUIRED = 6;
    public static final int NETWORK_ERROR = 7;
    public static final int INTERNAL_ERROR = 8;
    public static final int ERROR = 13;
    public static final int INTERRUPTED = 14;
    public static final int TIMEOUT = 15;
    public static final int CANCELED = 16;

    protected CommonStatusCodes() {
    }

    public static String getStatusCodeString(int statusCode) {
        switch (statusCode) {
            case 0:
                return "SUCCESS";
            case 6:
                return "RESOLUTION_REQUIRED";
            case 7:
                return "NETWORK_ERROR";
            case 8:
                return "INTERNAL_ERROR";
            case 13:
                return "ERROR";
            case 14:
                return "INTERRUPTED";
            case 15:
                return "TIMEOUT";
            case 16:
                return "CANCELED";
            default:
                return "Unknown status code: " + statusCode;
        }
    }
}
