package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.ppcp.model.BluetoothPackage;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.ppcp.model.IntervalOfCollectedPackages;
import org.coralibre.android.sdk.internal.database.ppcp.model.IntervalOfCollectedPackagesImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDatabase implements Database {
    private Map<ENNumber, IntervalOfCollectedPackages> collectedPackagesByInterval = new HashMap<>();
    private List<GeneratedTEK> generatedTEKs = new ArrayList<>();

    @Override
    public void addCollectedPayload(BluetoothPackage collectedPayload) {
        ENNumber interval = collectedPayload.getInterval();
        IntervalOfCollectedPackages payloadPerInterval = collectedPackagesByInterval.get(interval);
        if (payloadPerInterval == null) {
            payloadPerInterval = new IntervalOfCollectedPackagesImpl(interval);
            collectedPackagesByInterval.put(interval, payloadPerInterval);
        }
        payloadPerInterval.add(collectedPayload);
    }

    @Override
    public void addGeneratedTEK(GeneratedTEK generatedTEK) {
        generatedTEKs.add(generatedTEK);
    }

    @Override
    public Iterable<GeneratedTEK> getAllGeneratedTEKs() {
        return generatedTEKs;
    }

    @Override
    public Iterable<IntervalOfCollectedPackages> getAllCollectedPayload() {
        return collectedPackagesByInterval.values();

    }

    @Override
    public void truncateLast14Days() {
        ENNumber now = CryptoModule.getCurrentENNumber();
        long lastIntervalToKeep =now.get() -
                        (CryptoModule.TEK_MAX_STORE_TIME
                                * TemporaryExposureKey.TEK_ROLLING_PERIOD);

        List<ENNumber> toRemove = new ArrayList<>();
        for(ENNumber key : collectedPackagesByInterval.keySet()) {
            if(key.get() < lastIntervalToKeep) {
                toRemove.add(key);
            }
        }
        for(ENNumber key : toRemove) {
            collectedPackagesByInterval.remove(key);
        }

        for(int i = 0; i < generatedTEKs.size(); i++) {
            if(generatedTEKs.get(i).getInterval().get() < lastIntervalToKeep) {
                generatedTEKs.remove(i);
                i--;
            }
        }
    }

}
