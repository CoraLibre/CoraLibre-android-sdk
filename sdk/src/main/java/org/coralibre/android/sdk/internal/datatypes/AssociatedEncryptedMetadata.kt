package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class AssociatedEncryptedMetadata(rawAEM: ByteArray) {
    private val data: ByteArray

    init {
        if (rawAEM.size != EnFrameworkConstants.AEM_LENGTH) throw InvalidParameterException("wrong rawAEM size")
        data = rawAEM.copyOf()
    }

    fun getData(): ByteArray {
        val retVal = ByteArray(EnFrameworkConstants.AEM_LENGTH)
        System.arraycopy(data, 0, retVal, 0, EnFrameworkConstants.AEM_LENGTH)
        return retVal
    }
}
