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

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.ScanCallback
import android.content.Context
import org.coralibre.android.sdk.internal.BroadcastHelper.sendErrorUpdateBroadcast

class BluetoothServiceStatus private constructor(private val context: Context) {
    /**
     * @return [BluetoothServiceStatus.SCAN_OK] or error code (one of [ScanCallback].SCAN_FAILED_*) for scan failure.
     */
    var scanStatus = SCAN_OK
        private set

    /**
     * @return [BluetoothServiceStatus.ADVERTISE_OK] or error code
     * (see [AdvertiseCallback]#ADVERTISE_FAILED_* constants) for advertising start failures.
     */
    var advertiseStatus = ADVERTISE_OK
        private set

    /**
     * @param scanStatus [BluetoothServiceStatus.SCAN_OK] or error code (one of [ScanCallback]#SCAN_FAILED_*) for scan
     * failure.
     */
    fun updateScanStatus(scanStatus: Int) {
        if (this.scanStatus != scanStatus) {
            this.scanStatus = scanStatus
            sendErrorUpdateBroadcast(context)
        }
    }

    /**
     * @param advertiseStatus [BluetoothServiceStatus.ADVERTISE_OK] or error code
     * (see [AdvertiseCallback]#ADVERTISE_FAILED_* constants) for advertising start failures.
     */
    fun updateAdvertiseStatus(advertiseStatus: Int) {
        if (this.advertiseStatus != advertiseStatus) {
            this.advertiseStatus = advertiseStatus
            sendErrorUpdateBroadcast(context)
        }
    }

    companion object {
        const val SCAN_OK = 0
        const val ADVERTISE_OK = 0

        private var instance: BluetoothServiceStatus? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): BluetoothServiceStatus {
            if (instance == null) {
                instance = BluetoothServiceStatus(context.applicationContext)
            }
            return instance!!
        }

        @JvmStatic
        @Synchronized
        fun resetInstance() {
            instance = null
        }
    }
}
