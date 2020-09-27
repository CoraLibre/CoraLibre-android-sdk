package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.database.model.MeasuredExposure;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyProto;

import java.util.List;

public interface Database {

    void addGeneratedTEK(GeneratedTEK generatedTEK);
    void addCapturedPayload(CapturedData collectedPayload);

    void addDiagnosisKeys(List<TemporaryExposureKeyProto> diagnosisKeys);
    void updateDiagnosisKeys(List<TemporaryExposureKeyProto> diagnosisKeys);

    boolean hasTEKForInterval(ENInterval interval);

    /**
     * @param interval An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    GeneratedTEK getGeneratedTEK(ENInterval interval) throws StorageException;

    Iterable<GeneratedTEK> getAllGeneratedTEKs();
    Iterable<IntervalOfCapturedData> getAllCollectedPayload();

    List<MeasuredExposure> findAllMeasuredExposures();

    void truncateLast14Days();

    void clearAllData();
}
