package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;

public interface Database {
    void addCapturedPayload(CapturedData collectedPayload);
    void addGeneratedTEK(GeneratedTEK generatedTEK);

    Iterable<GeneratedTEK> getAllGeneratedTEKs();
    GeneratedTEK getGeneratedTEK(ENNumber interval);
    Iterable<IntervalOfCapturedData> getAllCollectedPayload();

    void truncateLast14Days();

    void clearAllData();
}
