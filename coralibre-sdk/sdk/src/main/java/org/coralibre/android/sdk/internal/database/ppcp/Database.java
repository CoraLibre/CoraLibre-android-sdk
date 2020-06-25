package org.coralibre.android.sdk.internal.database.ppcp;


import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.ppcp.model.BluetoothPackage;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEKImpl;
import org.coralibre.android.sdk.internal.database.ppcp.model.IntervalOfCollectedPackages;

public interface Database {
    void addCollectedPayload(BluetoothPackage collectedPayload);
    void addGeneratedTEK(GeneratedTEK generatedTEK);

    Iterable<GeneratedTEK> getAllGeneratedTEKs();
    GeneratedTEK getGeneratedTEK(ENNumber interval);
    Iterable<IntervalOfCollectedPackages> getAllCollectedPayload();

    void truncateLast14Days();
}
