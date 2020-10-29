package org.coralibre.android.sdk.internal.datatypes

import org.coralibre.android.sdk.internal.EnFrameworkConstants
import java.security.InvalidParameterException

class AssociatedEncryptedMetadata(rawAEM: ByteArray) {
    val data: ByteArray
        get() {
            val retVal = ByteArray(EnFrameworkConstants.AEM_LENGTH)
            System.arraycopy(field, 0, retVal, 0, EnFrameworkConstants.AEM_LENGTH)
            return retVal
        }

    init {
        if (rawAEM.size != EnFrameworkConstants.AEM_LENGTH) throw InvalidParameterException("wrong rawAEM size")
        data = rawAEM.copyOf()
    }
}
