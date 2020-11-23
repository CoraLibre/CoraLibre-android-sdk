/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import org.coralibre.android.sdk.internal.AppConfigManager
import org.coralibre.android.sdk.internal.BroadcastHelper
import org.coralibre.android.sdk.internal.TracingService
import org.coralibre.android.sdk.internal.database.DatabaseAccess

object PPCP {
    private const val TAG = "PPCP Interface"
    const val UPDATE_INTENT_ACTION = "org.coralibre.android.sdk.UPDATE_ACTION"
    private var isInitialized = false

    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        if (isInitialized) {
            return
        }
        DatabaseAccess.init(context)
        // TODO check: is this indeed the app context?
        executeInit(context)
        isInitialized = true
    }

    private fun executeInit(context: Context) {
        // TODO: Schedule the truncation to happen regularly and asynchronously instead of
        //  (only) performing it here.
        DatabaseAccess.getDefaultDatabaseInstance().truncateLast14Days()
        val appConfigManager = AppConfigManager.getInstance(context)
        val advertising = appConfigManager.isAdvertisingEnabled
        val receiving = appConfigManager.isReceivingEnabled
        if (advertising || receiving) {
            start(context, advertising, receiving)
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkInit() {
        check(isInitialized) { "You have to call PPCP.init() in your application onCreate()" }
    }

    @JvmStatic
    fun start(context: Context) {
        start(context, advertise = true, receive = true)
    }

    @JvmStatic
    internal fun start(context: Context, advertise: Boolean, receive: Boolean) {
        checkInit()
        val appConfigManager = AppConfigManager.getInstance(context)
        appConfigManager.isAdvertisingEnabled = advertise
        appConfigManager.isReceivingEnabled = receive
        val intent = Intent(context, TracingService::class.java)
            .setAction(TracingService.ACTION_START)
            .putExtra(TracingService.EXTRA_ADVERTISE, advertise)
            .putExtra(TracingService.EXTRA_RECEIVE, receive)
        ContextCompat.startForegroundService(context, intent)
        BroadcastHelper.sendUpdateBroadcast(context)
    }

    @JvmStatic
    fun isStarted(context: Context): Boolean {
        checkInit()
        val appConfigManager = AppConfigManager.getInstance(context)
        return appConfigManager.isAdvertisingEnabled || appConfigManager.isReceivingEnabled
    }

    @JvmStatic
    fun stop(context: Context) {
        checkInit()
        val appConfigManager = AppConfigManager.getInstance(context)
        appConfigManager.isAdvertisingEnabled = false
        appConfigManager.isReceivingEnabled = false
        val intent =
            Intent(context, TracingService::class.java).setAction(TracingService.ACTION_STOP)
        context.startService(intent)
        BroadcastHelper.sendUpdateBroadcast(context)
    }

    @JvmStatic
    val updateIntentFilter: IntentFilter
        get() = IntentFilter(UPDATE_INTENT_ACTION)

    @JvmStatic
    fun clearData(context: Context, onDeleteListener: Runnable?) {
        checkInit()
        val appConfigManager = AppConfigManager.getInstance(context)
        check(!(appConfigManager.isAdvertisingEnabled || appConfigManager.isReceivingEnabled)) { "Tracking must be stopped for clearing the local data" }
        appConfigManager.clearPreferences()
        DatabaseAccess.getDefaultDatabaseInstance().clearAllData()
    }
}
