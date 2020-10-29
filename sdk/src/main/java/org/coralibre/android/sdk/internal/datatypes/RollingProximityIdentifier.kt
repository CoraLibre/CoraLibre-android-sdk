package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class RollingProximityIdentifier(rawRPI: ByteArray, interval: ENInterval) {
    private val data: ByteArray
    val interval: ENInterval = ENInterval(interval)

    init {
        if (rawRPI.size != EnFrameworkConstants.RPI_LENGTH) throw InvalidParameterException("wrong rawRPI size")
        this.data = rawRPI.copyOf()
    }

    fun getData(): ByteArray {
        val retVal = ByteArray(EnFrameworkConstants.RPI_LENGTH)
        System.arraycopy(data, 0, retVal, 0, EnFrameworkConstants.RPI_LENGTH)
        return retVal
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RollingProximityIdentifier
        return data.contentEquals(that.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
