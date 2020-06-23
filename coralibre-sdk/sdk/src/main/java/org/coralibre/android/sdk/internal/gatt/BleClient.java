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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coralibre.android.sdk.internal.AppConfigManager;
import org.coralibre.android.sdk.internal.BroadcastHelper;
import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.BluetoothPayload;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.logger.Logger;

import static org.coralibre.android.sdk.internal.gatt.BleServer.SERVICE_UUID;

public class BleClient {

	private static final String TAG = "BleClient";

	private final Context context;
	private BluetoothLeScanner bleScanner;
	private ScanCallback bleScanCallback;
	private GattConnectionThread gattConnectionThread;

	// contains the received payload and the rx power
	private List<Pair<BluetoothPayload, Integer>> collectedPayload = new ArrayList<>();

	public BleClient(Context context) {
		this.context = context;
		gattConnectionThread = new GattConnectionThread();
		gattConnectionThread.start();
	}

	public BluetoothState start() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return bluetoothAdapter == null ? BluetoothState.NOT_SUPPORTED : BluetoothState.DISABLED;
		}
		bleScanner = bluetoothAdapter.getBluetoothLeScanner();
		if (bleScanner == null) {
			return BluetoothState.NOT_SUPPORTED;
		}

		List<ScanFilter> scanFilters = new ArrayList<>();
		scanFilters.add(new ScanFilter.Builder()
				.setServiceUuid(new ParcelUuid(SERVICE_UUID))
				.build());

		ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
				.setScanMode(AppConfigManager
						.getInstance(context)
						.getBluetoothScanMode()
						.getSystemValue())
				.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
				.setReportDelay(0)
				.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
				.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			settingsBuilder
					.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
					.setLegacy(true);
		}
		ScanSettings scanSettings = settingsBuilder.build();

		BluetoothServiceStatus bluetoothServiceStatus = BluetoothServiceStatus.getInstance(context);

		bleScanCallback = new ScanCallback() {
			private static final String TAG = "ScanCallback";

			public void onScanResult(int callbackType, ScanResult result) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				if (result.getScanRecord() != null) {
					onDeviceFound(result);
				}
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				Logger.d(TAG, "Batch size " + results.size());
				for (ScanResult result : results) {
					onScanResult(0, result);
				}
			}

			public void onScanFailed(int errorCode) {
				bluetoothServiceStatus.updateScanStatus(errorCode);
				Logger.e(TAG, "error: " + errorCode);
			}
		};

		bleScanner.startScan(scanFilters, scanSettings, bleScanCallback);
		Logger.i(TAG, "started BLE scanner, scanMode: " + scanSettings.getScanMode() + " scanFilters: " + scanFilters.size());

		return BluetoothState.ENABLED;
	}

	private void onDeviceFound(ScanResult scanResult) {
		try {
			final ENNumber now = CryptoModule.getCurrentENNumber();
			BluetoothDevice bluetoothDevice = scanResult.getDevice();
			final String deviceAddr = bluetoothDevice.getAddress();

			int rxPower = scanResult.getRssi();
			byte[] rawPayload = scanResult.getScanRecord().getServiceData(new ParcelUuid(SERVICE_UUID));
			BluetoothPayload payload = new BluetoothPayload(rawPayload, now);
			Logger.d(TAG, "found " + deviceAddr + "; rssi: " + scanResult.getRssi());
			for(Pair<BluetoothPayload, Integer> alreadyCollected : collectedPayload) {
				if(alreadyCollected.first.equals(payload))
					return;
			}
			collectedPayload.add(new Pair<>(payload, rxPower));
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	private synchronized void stopScan() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			bleScanner = null;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return;
		}
		if (bleScanner != null) {
			Logger.i(TAG, "stopping BLE scanner");
			bleScanner.stopScan(bleScanCallback);
			bleScanner = null;
		}
	}

	public synchronized void stop() {
		gattConnectionThread.terminate();
		stopScan();
		//TODO: Save collectedPayload into database
	}

}
