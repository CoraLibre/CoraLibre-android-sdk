package org.coralibre.android.sdk.internal.datatypes.util

import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyProto
import java.util.ArrayList

object DiagnosisKeyUtil {
    @JvmStatic
    fun toDiagnosisKeys(tekProtos: List<TemporaryExposureKeyProto>): List<DiagnosisKey> {
        val entityDiagnosisKeys: MutableList<DiagnosisKey> = ArrayList()
        for (tekProto in tekProtos) {
            require(tekProto.hasKeyData()) { "missing tekProto keyData" }
            require(tekProto.hasRollingStartIntervalNumber()) { "missing tekProto rollingStartIntervalNumber" }
            entityDiagnosisKeys.add(
                DiagnosisKey(
                    InternalTemporaryExposureKey(
                        ENInterval(tekProto.rollingStartIntervalNumber.toLong()),
                        tekProto.keyData.toByteArray()
                    ),
                    if (tekProto.hasTransmissionRiskLevel()) tekProto.transmissionRiskLevel else 0
                )
            )
        }
        return entityDiagnosisKeys
    }
}
