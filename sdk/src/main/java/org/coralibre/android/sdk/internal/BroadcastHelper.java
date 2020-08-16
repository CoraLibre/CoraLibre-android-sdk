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
import android.content.Intent;

import org.coralibre.android.sdk.PPCP;

public class BroadcastHelper {

	public static final String ACTION_UPDATE_ERRORS = "org.coralibre.android.sdk.internal.ACTION_UPDATE_ERRORS";

	public static void sendUpdateBroadcast(Context context) {
		Intent intent = new Intent(PPCP.UPDATE_INTENT_ACTION);
		context.sendBroadcast(intent);
	}

	public static void sendErrorUpdateBroadcast(Context context) {
		sendUpdateBroadcast(context);

		Intent intent = new Intent(ACTION_UPDATE_ERRORS);
		context.sendBroadcast(intent);
	}

}
