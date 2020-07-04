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
import android.database.sqlite.SQLiteException;
import androidx.annotation.NonNull;
import androidx.work.*;

import java.io.IOException;
import java.security.PublicKey;

import com.google.protobuf.InvalidProtocolBufferException;

import org.coralibre.android.sdk.TracingStatus.ErrorState;
import org.coralibre.android.sdk.internal.backend.proto.Exposed;
import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.logger.Logger;

/**
 * Originally retrieved diagnosis keys from backend and inserted them into backend.
 *
 * Not fully removed as the content of the {@link #doSyncInternal(Context)} method might still
 * be useful.
 */
// TODO: delete class
@Deprecated
public class SyncWorker extends Worker {

	private static final String TAG = "SyncWorker";
	private static final String WORK_TAG = "org.coralibre.android.sdk.internal.SyncWorker";

	@Deprecated
	public static void startSyncWorker(Context context) {
		// kept for compatibility
	}

	@Deprecated
	public static void stopSyncWorker(Context context) {
		WorkManager workManager = WorkManager.getInstance(context);
		workManager.cancelAllWorkByTag(WORK_TAG);
	}

	public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@Deprecated
	public static void setBucketSignaturePublicKey(PublicKey publicKey) {
		// kept for compatibility
	}

	@NonNull
	@Override
	public Result doWork() {
		Logger.d(TAG, "start SyncWorker");
		Context context = getApplicationContext();

		long scanInterval = AppConfigManager.getInstance(getApplicationContext()).getScanInterval();
		TracingService.scheduleNextClientRestart(context, scanInterval);
		TracingService.scheduleNextServerRestart(context);

		try {
			doSync(context);
		} catch (IOException | SQLiteException e) {
			Logger.d(TAG, "SyncWorker finished with exception " + e.getMessage());
			return Result.retry();
		}
		Logger.d(TAG, "SyncWorker finished with success");
		return Result.success();
	}

	public static void doSync(Context context)
			throws IOException,SQLiteException {
		try {
			doSyncInternal(context);
			Logger.i(TAG, "synced");
			AppConfigManager.getInstance(context).setLastSyncNetworkSuccess(true);
			//SyncErrorState.getInstance().setSyncError(null);
			BroadcastHelper.sendErrorUpdateBroadcast(context);
		} catch (IOException | SQLiteException e) {
			Logger.e(TAG, e);
			AppConfigManager.getInstance(context).setLastSyncNetworkSuccess(false);
			ErrorState syncError;
			if (e instanceof InvalidProtocolBufferException) {
				syncError = ErrorState.SYNC_ERROR_SERVER;
			} else if (e instanceof SQLiteException) {
				syncError = ErrorState.SYNC_ERROR_DATABASE;
			} else {
				syncError = ErrorState.SYNC_ERROR_NETWORK;
			}
			//SyncErrorState.getInstance().setSyncError(syncError);
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			throw e;
		}
	}

	private static void doSyncInternal(Context context) throws IOException {
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);

		// Random value to satisfy compiler and keep readability of code below
		final long BATCH_LENGTH = 16;

		Database database = new Database(context);
		database.generateContactsFromHandshakes(context);

		long lastLoadedBatchReleaseTime = appConfigManager.getLastLoadedBatchReleaseTime();
		long nextBatchReleaseTime;
		if (lastLoadedBatchReleaseTime <= 0 || lastLoadedBatchReleaseTime % BATCH_LENGTH != 0) {
			long now = System.currentTimeMillis();
			nextBatchReleaseTime = now - (now % BATCH_LENGTH);
		} else {
			nextBatchReleaseTime = lastLoadedBatchReleaseTime + BATCH_LENGTH;
		}

		for (long batchReleaseTime = nextBatchReleaseTime;
			 batchReleaseTime < System.currentTimeMillis();
			 batchReleaseTime += BATCH_LENGTH) {

			// Not valid now, will throw NPE; was retrieving exposees from backend.
			Exposed.ProtoExposedList result = null;
			// was before: result = backendBucketRepository.getExposees(batchReleaseTime)
			long batchReleaseServerTime = result.getBatchReleaseTime();
			for (Exposed.ProtoExposeeOrBuilder exposee : result.getExposedOrBuilderList()) {
				database.addKnownCase(
						context,
						exposee.getKey().toByteArray(),
						exposee.getKeyDate(),
						batchReleaseServerTime
				);
			}

			appConfigManager.setLastLoadedBatchReleaseTime(batchReleaseTime);
		}

		database.removeOldData();

		appConfigManager.setLastSyncDate(System.currentTimeMillis());
	}
}
