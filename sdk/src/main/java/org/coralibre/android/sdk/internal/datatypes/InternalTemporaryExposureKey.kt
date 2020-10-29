package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil
import java.security.InvalidParameterException

class InternalTemporaryExposureKey(interval: ENInterval, value: ByteArray) {
    val interval: ENInterval = ENIntervalUtil.getMidnight(interval)
    val key: ByteArray
        get() {
            val retVal = ByteArray(EnFrameworkConstants.TEK_LENGTH)
            System.arraycopy(field, 0, retVal, 0, EnFrameworkConstants.TEK_LENGTH)
            return retVal
        }

    init {
        if (value.size < EnFrameworkConstants.TEK_LENGTH) throw InvalidParameterException("tek size not 16 bytes")
        key = value.copyOf()
    }

    constructor(timestamp: Long, rawKey: ByteArray) : this(ENInterval(timestamp), rawKey)
}
