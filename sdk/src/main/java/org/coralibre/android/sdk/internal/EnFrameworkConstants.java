package org.coralibre.android.sdk.internal;


public class EnFrameworkConstants {


    // General:

    public static final int MINUTES_PER_DAY = 60 * 24;


    // EN framework specific:

    public static final int TEK_ROLLING_PERIOD = 144; //defined as 10min units; 144 units per day
    public static final int TEK_INTERVAL_LENGTH_MINUTES = MINUTES_PER_DAY / TEK_ROLLING_PERIOD;
    public static final int TEK_INTERVAL_LENGTH_SECONDS = 60 * TEK_INTERVAL_LENGTH_MINUTES;
    public static final int TEK_MAX_STORE_TIME_DAYS = 14; //defined as days
    public static final int TEK_MAX_STORE_TIME_MINUTES = MINUTES_PER_DAY * TEK_MAX_STORE_TIME_DAYS; //defined as days
    public static final int TEK_MAX_STORE_TIME_SECONDS = 60 * TEK_MAX_STORE_TIME_MINUTES; //defined as days
    public static final int TEK_MAX_STORE_TIME_INTERVALS = TEK_ROLLING_PERIOD * TEK_MAX_STORE_TIME_DAYS; //defined as days

    public static final int TEK_LENGTH = 16; //defined in byte
    public static final int RPIK_LENGTH = 16; // unit is bytes
    public static final int RPI_LENGTH = 16;
    public static final int AEMK_LENGTH = 16; // unit is bytes
    public static final int AEM_LENGTH = 4; //unit in bytes
    public static final int BLE_PAYLOAD_LENGTH = RPI_LENGTH + AEM_LENGTH;

    // TODO The following constant is just a guess! There might be documentation regarding this
    //  constant, but I did not look it up yet. For information regarding this constant see:
    //  https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/TracingParams.java#L52
    //  and line 200 in:
    //  https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java#L420
    //  Perhaps it should be 5 minutes? See line 203 in:
    //  https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java#L420
    /**
     * If two consecutive bluetooth payloads have been received for the same rpik, but the time
     * difference between receiving the two payloads is greater than the value defined here, the
     * payloads are treated as belonging to different 'Exposures'. Otherwise, both belong to the
     * same 'Exposure'.
     */
    public static final long MAX_EXPOSURE_INTERPOLATION_DURATION_SECONDS = 2 * TracingService.SCAN_INTERVAL;


}
