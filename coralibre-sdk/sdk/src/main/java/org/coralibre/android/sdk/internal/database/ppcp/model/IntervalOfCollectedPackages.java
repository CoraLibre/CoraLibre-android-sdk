package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

import java.util.List;

public interface IntervalOfCollectedPackages {

    ENNumber getInterval();

    List<BluetoothPackage> getBluetoothPackages();

    void add(BluetoothPackage bluetoothPackage);

}
