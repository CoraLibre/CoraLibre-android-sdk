package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

import java.util.ArrayList;
import java.util.List;

public class IntervalOfCollectedPackagesImpl implements IntervalOfCollectedPackages {

    ENNumber interval;
    List<BluetoothPackage> bluetoothPackages = new ArrayList<>();

    public IntervalOfCollectedPackagesImpl(ENNumber interval) {
        this.interval = interval;
    }

    public void add(BluetoothPackage bluetoothPackage) {
        bluetoothPackages.add(bluetoothPackage);
    }

    @Override
    public ENNumber getInterval() {
        return interval;
    }

    public List<BluetoothPackage> getBluetoothPackages() {
        return bluetoothPackages;
    }
}
