package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;

public interface Database {

    void addGeneratedTEK(GeneratedTEK generatedTEK);
    void addCapturedPayload(CapturedData collectedPayload);

    boolean hasTEKForInterval(ENInterval interval);

    /**
     * @param interval An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    GeneratedTEK getGeneratedTEK(ENInterval interval) throws StorageException;

    Iterable<GeneratedTEK> getAllGeneratedTEKs();
    Iterable<IntervalOfCapturedData> getAllCollectedPayload();
    Iterable<DiagnosisKey> getAllDiagnosisKeys();

    void truncateLast14Days();

    void clearAllData();
}
