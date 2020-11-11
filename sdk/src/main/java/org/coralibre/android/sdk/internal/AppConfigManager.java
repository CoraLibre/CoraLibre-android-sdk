/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal;

import android.content.Context;
import android.content.SharedPreferences;

import org.coralibre.android.sdk.internal.bluetooth.BluetoothScanMode;
import org.coralibre.android.sdk.internal.bluetooth.old.BluetoothAdvertiseMode;
import org.coralibre.android.sdk.internal.bluetooth.old.BluetoothTxPowerLevel;

public class AppConfigManager {

    private static AppConfigManager instance;

    public static synchronized AppConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppConfigManager(context);
        }
        return instance;
    }

    public static final int CALIBRATION_TEST_DEVICE_NAME_LENGTH = 4;

    public static final long DEFAULT_SCAN_INTERVAL = 1 * 60 * 1000L;
    public static final long DEFAULT_SCAN_DURATION = 20 * 1000L;
    private static final BluetoothScanMode DEFAULT_BLUETOOTH_SCAN_MODE = BluetoothScanMode.SCAN_MODE_LOW_POWER;
    private static final BluetoothTxPowerLevel DEFAULT_BLUETOOTH_POWER_LEVEL = BluetoothTxPowerLevel.ADVERTISE_TX_POWER_ULTRA_LOW;
    private static final BluetoothAdvertiseMode DEFAULT_BLUETOOTH_ADVERTISE_MODE = BluetoothAdvertiseMode.ADVERTISE_MODE_BALANCED;

    private static final String PREFS_NAME = "coralibre_sdk_preferences";
    private static final String PREF_ADVERTISING_ENABLED = "advertisingEnabled";
    private static final String PREF_RECEIVING_ENABLED = "receivingEnabled";
    private static final String PREF_CALIBRATION_TEST_DEVICE_NAME = "calibrationTestDeviceName";
    private static final String PREF_SCAN_INTERVAL = "scanInterval";
    private static final String PREF_SCAN_DURATION = "scanDuration";
    private static final String PREF_BLUETOOTH_SCAN_MODE = "scanMode";
    private static final String PREF_ADVERTISEMENT_POWER_LEVEL = "advertisementPowerLevel";
    private static final String PREF_ADVERTISEMENT_MODE = "advertisementMode";

    private SharedPreferences sharedPrefs;


    private AppConfigManager(Context context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    public void setAdvertisingEnabled(boolean enabled) {
        sharedPrefs.edit().putBoolean(PREF_ADVERTISING_ENABLED, enabled).apply();
    }

    public boolean isAdvertisingEnabled() {
        return sharedPrefs.getBoolean(PREF_ADVERTISING_ENABLED, false);
    }

    public void setReceivingEnabled(boolean enabled) {
        sharedPrefs.edit().putBoolean(PREF_RECEIVING_ENABLED, enabled).apply();
    }

    public boolean isReceivingEnabled() {
        return sharedPrefs.getBoolean(PREF_RECEIVING_ENABLED, false);
    }

    public void setCalibrationTestDeviceName(String name) {
        if (name != null && name.length() != CALIBRATION_TEST_DEVICE_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "CalibrationTestDevice Name must have length " + CALIBRATION_TEST_DEVICE_NAME_LENGTH + ", provided string '" +
                    name + "' with length " + name.length());
        }
        sharedPrefs.edit().putString(PREF_CALIBRATION_TEST_DEVICE_NAME, name).apply();
    }

    public String getCalibrationTestDeviceName() {
        return sharedPrefs.getString(PREF_CALIBRATION_TEST_DEVICE_NAME, null);
    }

    public void setScanDuration(long scanDuration) {
        sharedPrefs.edit().putLong(PREF_SCAN_DURATION, scanDuration).apply();
    }

    public long getScanDuration() {
        return sharedPrefs.getLong(PREF_SCAN_DURATION, DEFAULT_SCAN_DURATION);
    }

    public void setScanInterval(long scanInterval) {
        sharedPrefs.edit().putLong(PREF_SCAN_INTERVAL, scanInterval).apply();
    }

    public long getScanInterval() {
        return sharedPrefs.getLong(PREF_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL);
    }

    public void setBluetoothPowerLevel(BluetoothTxPowerLevel powerLevel) {
        sharedPrefs.edit().putInt(PREF_ADVERTISEMENT_POWER_LEVEL, powerLevel.ordinal()).apply();
    }

    public BluetoothTxPowerLevel getBluetoothTxPowerLevel() {
        return BluetoothTxPowerLevel.values()[sharedPrefs
            .getInt(PREF_ADVERTISEMENT_POWER_LEVEL, DEFAULT_BLUETOOTH_POWER_LEVEL.ordinal())];
    }

    public void setBluetoothScanMode(BluetoothScanMode scanMode) {
        sharedPrefs.edit().putInt(PREF_BLUETOOTH_SCAN_MODE, scanMode.ordinal()).apply();
    }

    public BluetoothScanMode getBluetoothScanMode() {
        return BluetoothScanMode.values()[sharedPrefs.getInt(PREF_BLUETOOTH_SCAN_MODE, DEFAULT_BLUETOOTH_SCAN_MODE.ordinal())];
    }

    public void setBluetoothAdvertiseMode(BluetoothAdvertiseMode advertiseMode) {
        sharedPrefs.edit().putInt(PREF_ADVERTISEMENT_MODE, advertiseMode.ordinal()).apply();
    }

    public BluetoothAdvertiseMode getBluetoothAdvertiseMode() {
        return BluetoothAdvertiseMode.values()[sharedPrefs
            .getInt(PREF_ADVERTISEMENT_MODE, DEFAULT_BLUETOOTH_ADVERTISE_MODE.ordinal())];
    }


    public void clearPreferences() {
        sharedPrefs.edit().clear().apply();
    }

}
