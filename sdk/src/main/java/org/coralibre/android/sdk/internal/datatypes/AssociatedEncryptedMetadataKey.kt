package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class AssociatedEncryptedMetadataKey(value: ByteArray) {
    private val key: ByteArray

    init {
        if (value.size != EnFrameworkConstants.AEMK_LENGTH) throw InvalidParameterException("size not 16bytes")
        key = value.copyOf()
    }

    fun getKey(): ByteArray {
        val retVal = ByteArray(EnFrameworkConstants.AEMK_LENGTH)
        System.arraycopy(key, 0, retVal, 0, EnFrameworkConstants.AEMK_LENGTH)
        return retVal
    }
}
