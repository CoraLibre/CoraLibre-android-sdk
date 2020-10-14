/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import org.coralibre.android.sdk.BuildConfig;
import org.coralibre.android.sdk.internal.AppConfigManager;
import org.coralibre.android.sdk.internal.crypto.CryptoModule;
import org.coralibre.android.sdk.internal.datatypes.AssociatedMetadata;
import org.coralibre.android.sdk.internal.device_info.DeviceInfo;
import org.coralibre.android.sdk.internal.device_info.DeviceList;

import java.io.IOException;
import java.util.UUID;

public class BleServer {

	private static final String TAG = "BleServer";

	private static final String PPCP_16_BIT_UUID = "FD6F";
	private static final int PPCP_VERSION_MAJOR = 1;
	private static final int PPCP_VERSION_MINOR = 0;

	public static final UUID SERVICE_UUID = UUID.fromString("0000" + PPCP_16_BIT_UUID + "-0000-1000-8000-00805F9B34FB");
	public static final UUID TOTP_CHARACTERISTIC_UUID = UUID.fromString("8c8494e3-bab5-1848-40a0-1b06991c0001");

	private final Context context;
	private final CryptoModule cryptoModule;

	private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartFailure(int errorCode) {
			Log.e(TAG, "advertise onStartFailure(), errorCode: " + errorCode);
			BluetoothServiceStatus.getInstance(context).updateAdvertiseStatus(errorCode);
		}

		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "advertise onStartSuccess(), settingsInEffect: " + settingsInEffect.toString());
			}
			BluetoothServiceStatus.getInstance(context).updateAdvertiseStatus(BluetoothServiceStatus.ADVERTISE_OK);
		}
	};


	private BluetoothAdapter mAdapter;
	private BluetoothLeAdvertiser mLeAdvertiser;

	public BleServer(Context context) {
		this.context = context;
		cryptoModule = CryptoModule.getInstance();
	}

	private byte[] getAdvertiseData() {
		return cryptoModule.getCurrentPayload().getRawPayload();
		//TODO: add data described in here:
		// https://covid19-static.cdn-apple.com/applications/covid19/current/static/contact-tracing/pdf/ExposureNotification-BluetoothSpecificationv1.2.pdf
	}

	public BluetoothState startAdvertising() {
		BluetoothManager mManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

		if (mManager == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			return BluetoothState.NOT_SUPPORTED;
		}

		mAdapter = mManager.getAdapter();
		mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
		if (mLeAdvertiser == null) {
			return BluetoothState.NOT_SUPPORTED;
		}

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);

		// This allowes to set the bluetooth parameters more detailed, however it seems
		// to only be available on phones with bluetooth 5.0 and API level 26+
		/*
		AdvertisingSetParameters advParameters = new AdvertisingSetParameters.Builder()
				.setTxPowerLevel(TX_POWER_LOW)
				.setInterval(INTERVAL_MEDIUM)
				.setIncludeTxPower(false)
				.setConnectable(false)
				.build();

		 */


		final AdvertiseSettings advSettings = new AdvertiseSettings.Builder()
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
				.setConnectable(false)
				.setTimeout(0)
				.build();


        final DeviceInfo deviceInfo;
        try {
            deviceInfo = DeviceList.getOwnDeviceInfo(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final char txPowerLevel = (char) deviceInfo.getTx();
        CryptoModule.getInstance().setMetadata(
            new AssociatedMetadata(PPCP_VERSION_MAJOR, PPCP_VERSION_MINOR, txPowerLevel));


		cryptoModule.renewPayload();
		final AdvertiseData advData = new AdvertiseData.Builder()
				.setIncludeDeviceName(false)
				.setIncludeTxPowerLevel(false)
				.addServiceUuid(new ParcelUuid(SERVICE_UUID))
				.addServiceData(new ParcelUuid(SERVICE_UUID), getAdvertiseData())
				.build();

		mLeAdvertiser.startAdvertising(advSettings, advData, advertiseCallback);
		if (BuildConfig.DEBUG) {
            Log.d(TAG, "started advertising (only advertiseData), powerLevel: "
                + ((int) txPowerLevel));
		}

		return BluetoothState.ENABLED;
	}

	public void stopAdvertising() {
		if (mLeAdvertiser != null) {
			mLeAdvertiser.stopAdvertising(advertiseCallback);
		}
	}

	public void stop() {
		stopAdvertising();
		mAdapter = null;
	}
}
