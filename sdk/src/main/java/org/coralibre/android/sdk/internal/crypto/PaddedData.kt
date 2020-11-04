package org.coralibre.android.sdk.internal.crypto

import org.coralibre.android.sdk.internal.datatypes.ENInterval
import java.nio.charset.StandardCharsets
import java.security.InvalidParameterException

class PaddedData {
    private val data = ByteArray(PADDED_DATA_SIZE)

    constructor(timestamp: ENInterval) {
        System.arraycopy(RPI_INFO.toByteArray(StandardCharsets.UTF_8), 0, data, 0, RPI_INFO.length)
        System.arraycopy(timestamp.bytes, 0, data, TIMESTAMP_POS, ENInterval.INT_BYTES)
    }

    constructor(rawPaddedData: ByteArray) {
        if (rawPaddedData.size != PADDED_DATA_SIZE) throw InvalidParameterException("wrong rawPaddedData size")
        System.arraycopy(rawPaddedData, 0, data, 0, PADDED_DATA_SIZE)
    }

    fun getData(): ByteArray {
        val retVal = ByteArray(PADDED_DATA_SIZE)
        System.arraycopy(data, 0, retVal, 0, PADDED_DATA_SIZE)
        return retVal
    }

    val isRPIInfoValid: Boolean
        get() {
            val extractedRpiInfo = ByteArray(RPI_INFO.length)
            System.arraycopy(data, 0, extractedRpiInfo, 0, RPI_INFO.length)
            return extractedRpiInfo.toString(Charsets.UTF_8) == RPI_INFO
        }

    val interval: ENInterval
        get() {
            val rawInterval = ByteArray(ENInterval.INT_BYTES)
            System.arraycopy(data, TIMESTAMP_POS, rawInterval, 0, ENInterval.INT_BYTES)
            return ENInterval(rawInterval)
        }

    companion object {
        // TODO In the following source file from the google code, padded data length is only 12?:
        // https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/cpp/constants.h

        const val RPI_INFO = "EN-RPI"
        const val PADDED_DATA_SIZE = 16
        const val TIMESTAMP_POS = 12
    }
}
