package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.internal.crypto.ENNumber;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;

public interface Database {

    void addGeneratedTEK(GeneratedTEK generatedTEK);
    void addCapturedPayload(CapturedData collectedPayload);

    boolean hasTEKForInterval(ENNumber interval);

    /**
     * @param interval  An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    GeneratedTEK getGeneratedTEK(ENNumber interval) throws StorageException;

    Iterable<GeneratedTEK> getAllGeneratedTEKs();
    Iterable<IntervalOfCapturedData> getAllCollectedPayload();

    void truncateLast14Days();

    void clearAllData();
}
