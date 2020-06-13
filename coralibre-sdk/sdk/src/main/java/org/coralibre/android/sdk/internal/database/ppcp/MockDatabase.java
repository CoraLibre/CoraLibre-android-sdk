package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.ppcp.models.CollectedPayload;
import org.coralibre.android.sdk.internal.database.ppcp.models.GeneratedTEK;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockDatabase implements Database {
    private Map<ENNumber, Set<CollectedPayload>> collectedPayloadByInterval = new HashMap<>();
    private Map<ENNumber, GeneratedTEK> generatedTEKs = new HashMap<>();

    @Override
    public void addCollectedPayload(CollectedPayload collectedPayload) {
        ENNumber interval = collectedPayload.getEnNumber();
        Set<CollectedPayload> slot = collectedPayloadByInterval.get(interval);
        if(slot == null) {
            slot = new HashSet<>();
            collectedPayloadByInterval.put(interval, slot);
        }
        slot.add(collectedPayload);
    }

    @Override
    public void addGeneratedTEK(GeneratedTEK generatedTEK) {
        ENNumber interval = generatedTEK.getTek().getInterval();
        GeneratedTEK storedTek = generatedTEKs.get(interval);
        if(storedTek == null) {
            generatedTEKs.put(interval, generatedTEK);
        } else {
            throw new StorageException("Trying to store new tek where one is already existing");
        }
    }

    @Override
    public Set<CollectedPayload> getCollectedPayloadByInterval(ENNumber enNumber) {
        Set<CollectedPayload> interval = collectedPayloadByInterval.get(enNumber);
        if(interval == null) {
            return Collections.emptySet();
        }
        return interval;
    }

    @Override
    public GeneratedTEK getGeneratedTEKByInterval(ENNumber enNumber) {
        GeneratedTEK genTEK = generatedTEKs.get(enNumber);
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
