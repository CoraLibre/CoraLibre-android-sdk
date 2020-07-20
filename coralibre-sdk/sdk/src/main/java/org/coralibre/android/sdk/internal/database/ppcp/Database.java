package org.coralibre.android.sdk.internal.database.ppcp;


import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.ppcp.model.CapturedData;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.ppcp.model.IntervalOfCapturedData;

public interface Database {
    void addCapturedPayload(CapturedData collectedPayload);
    void addGeneratedTEK(GeneratedTEK generatedTEK);

    Iterable<GeneratedTEK> getAllGeneratedTEKs();
    GeneratedTEK getGeneratedTEK(ENNumber interval);
    Iterable<IntervalOfCapturedData> getAllCollectedPayload();

    void truncateLast14Days();

    void clearAllData();
}
