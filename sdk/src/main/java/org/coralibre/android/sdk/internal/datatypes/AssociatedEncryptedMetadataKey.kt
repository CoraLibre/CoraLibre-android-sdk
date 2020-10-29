package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class AssociatedEncryptedMetadataKey(value: ByteArray) {
    val key: ByteArray
        get() {
            val retVal = ByteArray(EnFrameworkConstants.AEMK_LENGTH)
            System.arraycopy(field, 0, retVal, 0, EnFrameworkConstants.AEMK_LENGTH)
            return retVal
        }

    init {
        if (value.size != EnFrameworkConstants.AEMK_LENGTH) throw InvalidParameterException("size not 16bytes")
        key = value.copyOf()
    }
}
