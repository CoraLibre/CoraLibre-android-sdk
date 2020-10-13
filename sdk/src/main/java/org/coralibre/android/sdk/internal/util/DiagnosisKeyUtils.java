package org.coralibre.android.sdk.internal.util;

import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DiagnosisKeyUtils {


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
                    tekProto.getKeyData().toByteArray(),
                    tekProto.getRollingStartIntervalNumber(),
                    tekProto.hasTransmissionRiskLevel() ? tekProto.getTransmissionRiskLevel() : 0)
            );
        }
        return entityDiagnosisKeys;
    }


    public static List<EntityDiagnosisKey> toEntityDiagnosisKeys(String token, List<DiagnosisKey> diagKeys) {
        LinkedList<EntityDiagnosisKey> result = new LinkedList<EntityDiagnosisKey>();
        for (DiagnosisKey dk : diagKeys) {
            result.add(new EntityDiagnosisKey(token, dk));
        }
        return result;
    }


}
