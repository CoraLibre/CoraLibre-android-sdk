package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class RollingProximityIdentifierKey(value: ByteArray) {
    val key: ByteArray
        get() {
            val retVal = ByteArray(EnFrameworkConstants.RPIK_LENGTH)
            System.arraycopy(field, 0, retVal, 0, EnFrameworkConstants.RPIK_LENGTH)
            return retVal
        }

    init {
        if (value.size != EnFrameworkConstants.RPIK_LENGTH) throw InvalidParameterException("size not 16 bytes")
        key = value.copyOf()
    }

    // TODO: implement hashCode

    override fun equals(other: Any?): Boolean {
        return other is RollingProximityIdentifierKey && key.contentEquals(other.key)
    }
}
