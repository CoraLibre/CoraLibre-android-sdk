package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.BluetoothAdvertiseMode;
import org.coralibre.android.sdk.internal.crypto.ppcp.BluetoothPayload;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockDatabase implements Database {
    private Map<ENNumber, Set<BluetoothPayload>> collectedPayloadByInterval = new HashMap<>();
    private Map<ENNumber, TemporaryExposureKey> generatedTEKs = new HashMap<>();

    @Override
    public void addCollectedPayload(BluetoothPayload collectedPayload) {
        ENNumber interval = collectedPayload.getInterval();
        Set<BluetoothPayload> payloadPerInterval = collectedPayloadByInterval.get(interval);
        if(payloadPerInterval == null) {
            payloadPerInterval = new HashSet<>();
            collectedPayloadByInterval.put(interval, payloadPerInterval);
        }
        payloadPerInterval.add(collectedPayload);
    }

    @Override
    public void addGeneratedTEK(TemporaryExposureKey generatedTEK) {
        ENNumber interval = generatedTEK.getInterval();
        TemporaryExposureKey storedTek = generatedTEKs.get(interval);
        if(storedTek == null) {
            generatedTEKs.put(interval, generatedTEK);
        } else {
            throw new StorageException("Trying to store new tek where one is already existing");
        }
    }

    @Override
    public Set<BluetoothPayload> getCollectedPayloadByInterval(ENNumber enNumber) {
        Set<BluetoothPayload> interval = collectedPayloadByInterval.get(enNumber);
        if(interval == null) {
            return Collections.emptySet();
        }
        return interval;
    }

    @Override
    public TemporaryExposureKey getGeneratedTEKByInterval(ENNumber enNumber) {
        TemporaryExposureKey genTEK = generatedTEKs.get(enNumber);
        if(genTEK == null) {
            throw new StorageException("TEK for interval does not exist");
        }
        return genTEK;
    }

    @Override
    public boolean doesTEKExist(ENNumber enNumber) {
        return generatedTEKs.get(enNumber) != null;
    }


}
