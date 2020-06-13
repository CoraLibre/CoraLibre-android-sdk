package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.ppcp.models.CollectedPayload;
import org.coralibre.android.sdk.internal.database.ppcp.models.GeneratedTEK;

import java.util.Set;

public interface Database {
    void addCollectedPayload(CollectedPayload collectedPayload);
    void addGeneratedTEK(GeneratedTEK generatedTEK);

    Set<CollectedPayload> getCollectedPayloadByInterval(ENNumber enNumber);
    GeneratedTEK getGeneratedTEKByInterval(ENNumber enNumber);
    boolean doesTEKExist(ENNumber enNumber);
}
