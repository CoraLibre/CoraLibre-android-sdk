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
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import org.coralibre.android.sdk.BuildConfig
import org.coralibre.android.sdk.internal.AppConfigManager
import org.coralibre.android.sdk.internal.bluetooth.BluetoothServiceStatus.Companion.getInstance
import org.coralibre.android.sdk.internal.crypto.CryptoModule
import org.coralibre.android.sdk.internal.datatypes.AssociatedMetadata
import org.coralibre.android.sdk.internal.deviceinfo.DeviceInfo
import org.coralibre.android.sdk.internal.deviceinfo.DeviceList.Companion.getOwnDeviceInfo
import java.io.IOException
import java.util.UUID

class BleServer(private val context: Context) {
    private val cryptoModule: CryptoModule = CryptoModule.getInstance()
    private val advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "advertise onStartFailure(), errorCode: $errorCode")
            getInstance(context).updateAdvertiseStatus(errorCode)
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "advertise onStartSuccess(), settingsInEffect: $settingsInEffect")
            }
            getInstance(context).updateAdvertiseStatus(BluetoothServiceStatus.ADVERTISE_OK)
        }
    }
    private var mAdapter: BluetoothAdapter? = null
    private var mLeAdvertiser: BluetoothLeAdvertiser? = null

    private fun getAdvertiseData(): ByteArray {
        return cryptoModule.getCurrentPayload().rawPayload
        // TODO: add data described in here:
        // https://covid19-static.cdn-apple.com/applications/covid19/current/static/contact-tracing/pdf/ExposureNotification-BluetoothSpecificationv1.2.pdf
    }

    fun startAdvertising(): BluetoothState {
        val mManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if (mManager == null || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return BluetoothState.NOT_SUPPORTED
        }
        mAdapter = mManager.adapter
        mLeAdvertiser = mAdapter!!.bluetoothLeAdvertiser
        if (mLeAdvertiser == null) {
            return BluetoothState.NOT_SUPPORTED
        }
        val appConfigManager = AppConfigManager.getInstance(context)

        // This allows to set the bluetooth parameters more detailed, however it seems
        // to only be available on phones with bluetooth 5.0 and API level 26+

        /*
        AdvertisingSetParameters advParameters = new AdvertisingSetParameters.Builder()
            .setTxPowerLevel(TX_POWER_LOW)
            .setInterval(INTERVAL_MEDIUM)
            .setIncludeTxPower(false)
            .setConnectable(false)
            .build();
        */
        val advSettings = AdvertiseSettings.Builder()
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(false)
            .setTimeout(0)
            .build()
        val deviceInfo: DeviceInfo
        deviceInfo = try {
            getOwnDeviceInfo(context)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val txPowerLevel = deviceInfo.tx
        CryptoModule.getInstance().metadata =
            AssociatedMetadata(PPCP_VERSION_MAJOR, PPCP_VERSION_MINOR, txPowerLevel)
        cryptoModule.renewPayload()
        val advData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(SERVICE_UUID), getAdvertiseData())
            .build()
        mLeAdvertiser!!.startAdvertising(advSettings, advData, advertiseCallback)
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "started advertising (only advertiseData), powerLevel: $txPowerLevel"
            )
        }
        return BluetoothState.ENABLED
    }

    fun stopAdvertising() {
        if (mLeAdvertiser != null) {
            mLeAdvertiser!!.stopAdvertising(advertiseCallback)
        }
    }

    fun stop() {
        stopAdvertising()
        mAdapter = null
    }

    companion object {
        private const val TAG = "BleServer"
        private const val PPCP_16_BIT_UUID = "FD6F"
        private const val PPCP_VERSION_MAJOR = 1
        private const val PPCP_VERSION_MINOR = 0

        @JvmStatic
        val SERVICE_UUID = UUID.fromString("0000$PPCP_16_BIT_UUID-0000-1000-8000-00805F9B34FB")

        @JvmStatic
        val TOTP_CHARACTERISTIC_UUID = UUID.fromString("8c8494e3-bab5-1848-40a0-1b06991c0001")
    }
}
