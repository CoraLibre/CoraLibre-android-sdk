/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.annotation.RequiresApi;

import java.util.UUID;

import org.coralibre.android.sdk.BuildConfig;
import org.coralibre.android.sdk.internal.AppConfigManager;
import org.coralibre.android.sdk.internal.crypto.ppcp.AssociatedMetadata;
import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.logger.Logger;

import static android.bluetooth.le.AdvertisingSetParameters.INTERVAL_MEDIUM;

public class BleServer {

	private static final String TAG = "BleServer";

	private static final String PPCP_16_BIT_UUID = "FD6F";
	private static final int PPCP_VERSION_MAJOR = 1;
	private static final int PPCP_VERSION_MINOR = 0;

	public static final UUID SERVICE_UUID = UUID.fromString("0000" + PPCP_16_BIT_UUID + "-0000-1000-8000-00805F9B34FB");
	public static final UUID TOTP_CHARACTERISTIC_UUID = UUID.fromString("8c8494e3-bab5-1848-40a0-1b06991c0001");

	private final Context context;
	private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartFailure(int errorCode) {
			Logger.e(TAG, "advertise onStartFailure: " + errorCode);
			BluetoothServiceStatus.getInstance(context).updateAdvertiseStatus(errorCode);
		}

		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			Logger.i(TAG, "advertise onStartSuccess: " + settingsInEffect.toString());
			BluetoothServiceStatus.getInstance(context).updateAdvertiseStatus(BluetoothServiceStatus.ADVERTISE_OK);
		}
	};

	private BluetoothAdapter mAdapter;
	private BluetoothLeAdvertiser mLeAdvertiser;

	public BleServer(Context context) {
		this.context = context;
	}

	private byte[] getAdvertiseData() {
		CryptoModule cryptoModule = CryptoModule.getInstance(context);
		byte[] advertiseData = cryptoModule.getCurrentPayload().getRawPayload();
		//TODO: add data described in here:
		// https://covid19-static.cdn-apple.com/applications/covid19/current/static/contact-tracing/pdf/ExposureNotification-BluetoothSpecificationv1.2.pdf
		return advertiseData;
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

		//TODO: ADD REAL POWERLEVEL !!!! THIS IS JUST A PLACEHOLDER
		CryptoModule.getInstance(context).setMetadata(
				new AssociatedMetadata(PPCP_VERSION_MAJOR, PPCP_VERSION_MINOR, 0));

		AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
		settingBuilder.setAdvertiseMode(appConfigManager.getBluetoothAdvertiseMode().getSystemValue());
		settingBuilder.setTxPowerLevel(appConfigManager.getBluetoothTxPowerLevel().getSystemValue());
		settingBuilder.setConnectable(false);
		settingBuilder.setTimeout(0);
		AdvertiseSettings settings = settingBuilder.build();

		AdvertiseData.Builder advBuilder = new AdvertiseData.Builder();
		advBuilder.setIncludeTxPowerLevel(false);
		advBuilder.setIncludeDeviceName(false);
		advBuilder.addServiceUuid(new ParcelUuid(SERVICE_UUID));

		advBuilder.addServiceData(new ParcelUuid(SERVICE_UUID), getAdvertiseData());

		mLeAdvertiser.startAdvertising(settings, advBuilder.build(), advertiseCallback);
		Logger.d(TAG, "started advertising (only advertiseData), advertiseMode " + settings.getMode() + " powerLevel " +
				settings.getTxPowerLevel());

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
