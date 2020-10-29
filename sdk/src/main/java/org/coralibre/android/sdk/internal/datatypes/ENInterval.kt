package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidParameterException
import java.util.Objects

class ENInterval {
    private val value: Long

    constructor(enInterval: ENInterval) {
        value = enInterval.get()
    }

    constructor(rawENNumber: Long) {
        value = toEnIntervalNumberChecked(rawENNumber, false)
    }

    constructor(input: Long, isUnixTime: Boolean) {
        value = toEnIntervalNumberChecked(input, isUnixTime)
    }

    constructor(rawENNumber: ByteArray) {
        if (rawENNumber.size != INT_BYTES) throw InvalidParameterException("rawENNumber has wrong size")
        val rawBuffer = ByteArray(LONG_BYTES)
        System.arraycopy(rawENNumber, 0, rawBuffer, 0, INT_BYTES)
        val buffer = ByteBuffer.wrap(rawBuffer)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        value = toEnIntervalNumberChecked(buffer.long, false)
    }

    private fun toEnIntervalNumberChecked(input: Long, isUnixTime: Boolean): Long {
        val result = if (isUnixTime) {
            ENIntervalUtil.intervalNumberFromUnixTimestamp(input)
        } else {
            input
        }

        if (result < 0 || MAX_UINT_32 < result) {
            throw InvalidParameterException(
                "ENIntervalTimestamp out of bound: " + String.format("0x%X", result)
            )
        }
        return result
    }

    fun get(): Long {
        return value
    }

    val unixTime: Long
        get() = ENIntervalUtil.intervalNumberToUnixTimestamp(value)

    // This has to output the value as Little endian
    val bytes: ByteArray
        get() {
            // This has to output the value as Little endian
            val buffer = ByteBuffer.allocate(LONG_BYTES)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putLong(value)
            val retVal = ByteArray(INT_BYTES)
            System.arraycopy(buffer.array(), 0, retVal, 0, INT_BYTES)
            return retVal
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val enInterval = other as ENInterval
        return value == enInterval.value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        const val MAX_UINT_32 = 4294967295L
        const val LONG_BYTES = 8
        const val INT_BYTES = 4
    }
}
