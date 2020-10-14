package org.coralibre.android.sdk.internal.datatypes.util;

import org.coralibre.android.sdk.internal.datatypes.ENInterval;

import static org.coralibre.android.sdk.internal.EnFrameworkConstants.TEK_INTERVAL_LENGTH_SECONDS;
import static org.coralibre.android.sdk.internal.EnFrameworkConstants.TEK_ROLLING_PERIOD;

public class ENIntervalUtil {

    public static ENInterval getMidnight(final ENInterval enInterval) {
        return new ENInterval( ((long)(enInterval.get() / TEK_ROLLING_PERIOD)) * TEK_ROLLING_PERIOD);
    }

    public static long getMidnight(final long rawENNumber) {
        return ((rawENNumber/ TEK_ROLLING_PERIOD) * TEK_ROLLING_PERIOD);
    }

    public static ENInterval createFromUnixTimestamp(long unixtime) {
        return new ENInterval( unixtime / TEK_INTERVAL_LENGTH_SECONDS );
    }

    public static long intervalNumberFromUnixTimestamp(long unixtime) {
        return unixtime / TEK_INTERVAL_LENGTH_SECONDS;
    }

    public static long intervalNumberToUnixTimestamp(long interval) {
        return interval * TEK_INTERVAL_LENGTH_SECONDS;
    }

    public static ENInterval getCurrentInterval() {
        return new ENInterval(System.currentTimeMillis() / 1000L, true);
    }

}
