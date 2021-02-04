/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import org.coralibre.android.sdk.BuildConfig
import org.coralibre.android.sdk.internal.AppConfigManager
import org.coralibre.android.sdk.internal.BroadcastHelper.sendErrorUpdateBroadcast
import org.coralibre.android.sdk.internal.bluetooth.BluetoothServiceStatus.Companion.getInstance
import org.coralibre.android.sdk.internal.database.Database
import org.coralibre.android.sdk.internal.datatypes.BluetoothPayload
import org.coralibre.android.sdk.internal.datatypes.CapturedData
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.currentInterval
import org.coralibre.android.sdk.internal.util.ByteToHex.toString
import java.util.ArrayList

class BleClient(private val context: Context, private val database: Database) {
    private var bleScanner: BluetoothLeScanner? = null
    private var bleScanCallback: ScanCallback? = null

    // contains the received payload and the rx power
    private val collectedData: MutableList<CollectedDatumInstance> = ArrayList()

    private class CollectedDatumInstance(
        val payload: BluetoothPayload,
        val rssi: Byte,
        val timestamp: Long
    )

    fun start(): BluetoothState {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            sendErrorUpdateBroadcast(context)
            return if (bluetoothAdapter == null) BluetoothState.NOT_SUPPORTED
            else BluetoothState.DISABLED
        }
        bleScanner = bluetoothAdapter.bluetoothLeScanner
        if (bleScanner == null) {
            return BluetoothState.NOT_SUPPORTED
        }
        val scanFilters: MutableList<ScanFilter> = ArrayList()
        scanFilters.add(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(BleServer.SERVICE_UUID))
                .build()
        )
        val settingsBuilder = ScanSettings.Builder()
            .setScanMode(
                AppConfigManager
                    .getInstance(context)
                    .bluetoothScanMode
                    .systemValue
            )
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setReportDelay(0)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settingsBuilder
                .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                .setLegacy(true)
        }
        val scanSettings = settingsBuilder.build()
        val bluetoothServiceStatus = getInstance(
            context
        )
        bleScanCallback = object : ScanCallback() {
            val TAG = "ScanCallback"
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onScanResult()")
                }
                if (result.scanRecord != null) {
                    onDeviceFound(result)
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onBatchScanResults(), size: ${results.size}")
                }
                for (result in results) {
                    onScanResult(0, result)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                bluetoothServiceStatus.updateScanStatus(errorCode)
                Log.e(TAG, "onScanFailed(), errorCode: $errorCode")
            }
        }
        bleScanner!!.startScan(scanFilters, scanSettings, bleScanCallback)
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "started BLE scanner, " +
                    "scanMode: ${scanSettings.scanMode} scanFilters: ${scanFilters.size}"
            )
        }
        return BluetoothState.ENABLED
    }

    private fun onDeviceFound(scanResult: ScanResult) {
        try {
            val now = System.currentTimeMillis()
            val currentInterval = currentInterval
            val bluetoothDevice = scanResult.device
            val deviceAddr = bluetoothDevice.address
            val rssi = scanResult.rssi.toByte()
            val rawPayload =
                scanResult.scanRecord!!.getServiceData(ParcelUuid(BleServer.SERVICE_UUID))
            val payload = BluetoothPayload(rawPayload!!, currentInterval)
            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "found $deviceAddr; rssi: ${scanResult.rssi}; " +
                        "data: [${toString(rawPayload)}]; size: ${rawPayload.size}"
                )
            }

            // no we are not checking for duplicates. We need duplicates for the
            // risk calculation later
            collectedData.add(CollectedDatumInstance(payload, rssi, now))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun stopScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            bleScanner = null
            sendErrorUpdateBroadcast(context)
            return
        }
        if (bleScanner != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "stopping BLE scanner")
            }
            bleScanner!!.stopScan(bleScanCallback)
            bleScanner = null
        }
    }

    @Synchronized
    fun stop() {
        stopScan()
        for (data in collectedData) {
            database.addCapturedPayload(
                CapturedData(
                    data.timestamp,
                    data.rssi,
                    data.payload.rpi,
                    data.payload.aem
                )
            )
        }
    }

    companion object {
        private const val TAG = "BleClient"
    }
}
