package org.coralibre.android.sdk.internal.datatypes.util;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.TemporaryExposureKey_internal;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DiagnosisKeyUtil {


    public static List<DiagnosisKey> toDiagnosisKeys(List<TemporaryExposureKeyFile.TemporaryExposureKeyProto> tekProtos) {
        List<DiagnosisKey> entityDiagnosisKeys = new ArrayList<>();
        for (TemporaryExposureKeyFile.TemporaryExposureKeyProto tekProto : tekProtos) {
            if (!tekProto.hasKeyData()) {
                throw new IllegalArgumentException("missing tekProto keyData");
            } else if (!tekProto.hasRollingStartIntervalNumber()) {
                throw new IllegalArgumentException("missing tekProto rollingStartIntervalNumber");
            } else if (!tekProto.hasRollingPeriod()) {
                throw new IllegalArgumentException("missing tekProto rollingPeriod");
            }

            entityDiagnosisKeys.add(
                new DiagnosisKey(
                    new TemporaryExposureKey_internal(
                        new ENInterval(tekProto.getRollingStartIntervalNumber()),
                        tekProto.getKeyData().toByteArray()),
                    tekProto.hasTransmissionRiskLevel() ? tekProto.getTransmissionRiskLevel() : 0)
            );
        }
        return entityDiagnosisKeys;
    }

}
