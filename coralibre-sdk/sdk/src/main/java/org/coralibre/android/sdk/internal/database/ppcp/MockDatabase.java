package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.ppcp.models.Exposure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockDatabase implements Database {
    private Map<ENNumber, Set<Exposure>> exposuresBySlot = new HashMap<>();

    @Override
    public void addExposure(Exposure exposure) {
        ENNumber enNumber = exposure.getEnNumber();
        Set<Exposure> slot = exposuresBySlot.get(enNumber);
        if(slot == null) {
            slot = new HashSet<>();
            exposuresBySlot.put(enNumber, slot);
        }
        if(!slot.contains(exposure)) {
            slot.add(exposure);
        }
    }

    @Override
    public Set<Exposure> getExposuresBySlot(ENNumber enNumber) {
        Set<Exposure> slot = exposuresBySlot.get(enNumber);
        if(slot == null) {
            return Collections.emptySet();
        }
        return slot;
    }

}
