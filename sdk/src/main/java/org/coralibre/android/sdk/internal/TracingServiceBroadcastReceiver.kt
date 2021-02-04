/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import org.coralibre.android.sdk.BuildConfig

class TracingServiceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, i: Intent) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "received broadcast to start service")
        }
        val intent = Intent(context, TracingService::class.java).setAction(i.action)
        ContextCompat.startForegroundService(context, intent)
    }

    companion object {
        const val TAG = "TS BroadcastReceiver" // 23 chars is the maximum length
    }
}
