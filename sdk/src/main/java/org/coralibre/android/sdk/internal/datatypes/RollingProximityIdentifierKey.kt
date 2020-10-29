package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class RollingProximityIdentifierKey(value: ByteArray) {
    private val key: ByteArray

    init {
        if (value.size != EnFrameworkConstants.RPIK_LENGTH) throw InvalidParameterException("size not 16 bytes")
        key = value.copyOf()
    }

    fun getKey(): ByteArray {
        val retVal = ByteArray(EnFrameworkConstants.RPIK_LENGTH)
        System.arraycopy(key, 0, retVal, 0, EnFrameworkConstants.RPIK_LENGTH)
        return retVal
    }

    // TODO: implement hashCode

    override fun equals(other: Any?): Boolean {
        return other is RollingProximityIdentifierKey && key.contentEquals(other.key)
    }
}
