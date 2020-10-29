package org.coralibre.android.sdk.internal.datatypes.util

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import org.coralibre.android.sdk.internal.datatypes.ENInterval

object ENIntervalUtil {
    @JvmStatic
    fun getMidnight(enInterval: ENInterval): ENInterval {
        return ENInterval(
            (enInterval.get() / EnFrameworkConstants.TEK_ROLLING_PERIOD) * EnFrameworkConstants.TEK_ROLLING_PERIOD
        )
    }

    @JvmStatic
    fun getMidnight(rawENNumber: Long): Long {
        return rawENNumber / EnFrameworkConstants.TEK_ROLLING_PERIOD * EnFrameworkConstants.TEK_ROLLING_PERIOD
    }

    @JvmStatic
    fun createFromUnixTimestamp(unixtime: Long): ENInterval {
        return ENInterval(unixtime / EnFrameworkConstants.TEK_INTERVAL_LENGTH_SECONDS)
    }

    @JvmStatic
    fun intervalNumberFromUnixTimestamp(unixtime: Long): Long {
        return unixtime / EnFrameworkConstants.TEK_INTERVAL_LENGTH_SECONDS
    }

    @JvmStatic
    fun intervalNumberToUnixTimestamp(interval: Long): Long {
        return interval * EnFrameworkConstants.TEK_INTERVAL_LENGTH_SECONDS
    }

    @JvmStatic
    val currentInterval: ENInterval
        get() = ENInterval(System.currentTimeMillis() / 1000L, true)
}
