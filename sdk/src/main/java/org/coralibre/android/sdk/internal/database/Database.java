package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.crypto.TemporaryExposureKey_internal;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyProto;

import java.util.List;

public interface Database {

    void addGeneratedTEK(TemporaryExposureKey_internal generatedTEK);
    void addCapturedPayload(CapturedData collectedPayload);

    void addDiagnosisKeys(List<TemporaryExposureKeyProto> diagnosisKeys);
    void updateDiagnosisKeys(List<TemporaryExposureKeyProto> diagnosisKeys);

    List<DiagnosisKey> getAllDiagnosisKeys();

    boolean hasTEKForInterval(ENInterval interval);

    /**
     * @param interval An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    TemporaryExposureKey_internal getOwnTEK(ENInterval interval) throws StorageException;

    Iterable<TemporaryExposureKey_internal> getAllOwnTEKs();
    Iterable<IntervalOfCapturedData> getAllCollectedPayload();

    void truncateLast14Days();

    void clearAllData();
}
