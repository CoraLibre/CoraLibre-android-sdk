package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.ppcp.model.CapturedData;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.ppcp.model.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.database.ppcp.model.IntervalOfCapturedDataImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDatabase implements Database {
    private Map<ENNumber, IntervalOfCapturedData> collectedPackagesByInterval = new HashMap<>();
    private List<GeneratedTEK> generatedTEKs = new ArrayList<>();

    private static MockDatabase database = null;

    public static MockDatabase getInstance() {
        if (database == null)
            database = new MockDatabase();
        return database;
    }

    private MockDatabase() {

    }

    @Override
    public void addCapturedPayload(CapturedData collectedPayload) {
        long timestamp = collectedPayload.getCaptureTimestamp();
        ENNumber interval = new ENNumber(timestamp, true);

        // find correct interval
        IntervalOfCapturedData payloadPerInterval =
                collectedPackagesByInterval.get(interval);
        if (payloadPerInterval == null) {
            payloadPerInterval = new IntervalOfCapturedDataImpl(interval);
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
    public GeneratedTEK getGeneratedTEK(ENNumber interval) {
        for (GeneratedTEK tek : generatedTEKs) {
            if (tek.getInterval().equals(interval))
                return tek;
        }
        return null;
    }

    @Override
    public Iterable<IntervalOfCapturedData> getAllCollectedPayload() {
        return collectedPackagesByInterval.values();

    }

    @Override
    public void truncateLast14Days() {
        ENNumber now = CryptoModule.getCurrentInterval();
        long lastIntervalToKeep = now.get() -
                (CryptoModule.TEK_MAX_STORE_TIME
                        * TemporaryExposureKey.TEK_ROLLING_PERIOD);

        List<ENNumber> toRemove = new ArrayList<>();
        for (ENNumber key : collectedPackagesByInterval.keySet()) {
            if (key.get() < lastIntervalToKeep) {
                toRemove.add(key);
            }
        }
        for (ENNumber key : toRemove) {
            collectedPackagesByInterval.remove(key);
        }

        for (int i = 0; i < generatedTEKs.size(); i++) {
            if (generatedTEKs.get(i).getInterval().get() < lastIntervalToKeep) {
                generatedTEKs.remove(i);
                i--;
            }
        }
    }

}
