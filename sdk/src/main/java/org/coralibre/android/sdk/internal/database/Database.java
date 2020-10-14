package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.datatypes.TemporaryExposureKey_internal;

import java.util.List;

public interface Database {


    void addGeneratedTEK(TemporaryExposureKey_internal generatedTEK);

    Iterable<TemporaryExposureKey_internal> getAllOwnTEKs();

    /**
     * @param interval An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    TemporaryExposureKey_internal getOwnTEK(ENInterval interval) throws StorageException;

    boolean hasTEKForInterval(ENInterval interval);



    void addCapturedPayload(CapturedData collectedPayload);

    Iterable<IntervalOfCapturedData> getAllCollectedPayload();


    /**
     * @param token A token to identify the key set later on. If the token has already been used
     *              previously to add diagnosis keys, it now identifies the previously existing
     *              keys together with the new ones contained in 'diagnosisKeys'.
     */
    void addDiagnosisKeys(String token, List<DiagnosisKey> diagnosisKeys);

    void updateDiagnosisKeys(String token, List<DiagnosisKey> diagnosisKeys);

    /**
     * @param token A token with that an addDiagnosisKeys(...) call has been performed previously
     * @throws StorageException if the token is unknown to the db
     */
    List<DiagnosisKey> getDiagnosisKeys(String token) throws StorageException;

    /**
     * @param token A token with that an addDiagnosisKeys(...) call has been performed previously
     * @throws StorageException if the token is unknown to the db
     */
    void deleteTokenWithDiagnosisKeys(String token) throws StorageException;



    void truncateLast14Days();

    void clearAllData();
}
