/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import org.coralibre.android.sdk.internal.AppConfigManager;
import org.coralibre.android.sdk.internal.BroadcastHelper;
import org.coralibre.android.sdk.internal.TracingService;
import org.coralibre.android.sdk.internal.database.DatabaseAccess;
import org.coralibre.android.sdk.internal.util.ProcessUtil;

import java.security.PublicKey;

public class PPCP {

	private static final String TAG = "PPCP Interface";

	public static final String UPDATE_INTENT_ACTION = "org.coralibre.android.sdk.UPDATE_ACTION";

	private static boolean isInitialized = false;

	public static void init(Context context) {
		// TODO: there's no else branch, that's bad.
		if (ProcessUtil.isMainProcess(context)) {
			DatabaseAccess.init(context);
				// TODO check: is this indeed the app context?
			executeInit(context);
			PPCP.isInitialized = true;
		}
	}

	@Deprecated
	public static void init(Context context, String appId, PublicKey signaturePublicKey) {
		init(context);
	}

	@Deprecated
	public static void init(Context context, String appId, boolean enableDevDiscoveryMode, PublicKey signaturePublicKey) {
		init(context);
	}

	private static void executeInit(Context context) {
		DatabaseAccess.getDefaultDatabaseInstance().truncateLast14Days();
			// TODO: Schedule the truncation to happen regularly and asynchronously instead of
			//  (only) performing it here.

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		boolean advertising = appConfigManager.isAdvertisingEnabled();
		boolean receiving = appConfigManager.isReceivingEnabled();
		if (advertising || receiving) {
			start(context, advertising, receiving);
		}
	}

	private static void checkInit() throws IllegalStateException {
		if (!PPCP.isInitialized) {
			throw new IllegalStateException("You have to call PPCP.init() in your application onCreate()");
		}
	}

	public static void start(Context context) {
		start(context, true, true);
	}

	protected static void start(Context context, boolean advertise, boolean receive) {
		checkInit();
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setAdvertisingEnabled(advertise);
		appConfigManager.setReceivingEnabled(receive);
		Intent intent = new Intent(context, TracingService.class).setAction(TracingService.ACTION_START);
		intent.putExtra(TracingService.EXTRA_ADVERTISE, advertise);
		intent.putExtra(TracingService.EXTRA_RECEIVE, receive);
		ContextCompat.startForegroundService(context, intent);
		BroadcastHelper.sendUpdateBroadcast(context);
	}

	public static boolean isStarted(Context context) {
		checkInit();
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		return appConfigManager.isAdvertisingEnabled() || appConfigManager.isReceivingEnabled();
	}
	
	//public static TracingStatus getStatus(Context context) {
		// TODO: Implement
	//}

	public static void stop(Context context) {
		checkInit();

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setAdvertisingEnabled(false);
		appConfigManager.setReceivingEnabled(false);

		Intent intent = new Intent(context, TracingService.class).setAction(TracingService.ACTION_STOP);
		context.startService(intent);
		BroadcastHelper.sendUpdateBroadcast(context);
	}

	public static IntentFilter getUpdateIntentFilter() {
		return new IntentFilter(PPCP.UPDATE_INTENT_ACTION);
	}

	public static void clearData(Context context, Runnable onDeleteListener) {
		checkInit();
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		if (appConfigManager.isAdvertisingEnabled() || appConfigManager.isReceivingEnabled()) {
			throw new IllegalStateException("Tracking must be stopped for clearing the local data");
		}

		appConfigManager.clearPreferences();

		DatabaseAccess.getDefaultDatabaseInstance().clearAllData();
	}

}
