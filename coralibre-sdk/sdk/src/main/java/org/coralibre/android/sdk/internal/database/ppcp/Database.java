package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.BluetoothPayload;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;

import java.util.List;
import java.util.Set;

public interface Database {
    void addCollectedPayload(BluetoothPayload collectedPayload);
    void addGeneratedTEK(TemporaryExposureKey generatedTEK);

    Set<BluetoothPayload> getCollectedPayloadByInterval(ENNumber enNumber);
    TemporaryExposureKey getGeneratedTEKByInterval(ENNumber enNumber);
    boolean doesTEKExist(ENNumber enNumber);
    List<TemporaryExposureKey> getAllGeneratedTEKs();
    List<Set<BluetoothPayload>> getAllCollectedPayload();


    void truncateLast14Days();
}
