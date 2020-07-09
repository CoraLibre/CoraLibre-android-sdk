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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;


public class TracingServiceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "TracingServiceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d(TAG, "received broadcast to start service");
        Intent intent = new Intent(context, TracingService.class).setAction(i.getAction());
        intent.putExtra(TracingService.EXTRA_ADVERTISE, true);
        intent.putExtra(TracingService.EXTRA_RECEIVE, true);
        ContextCompat.startForegroundService(context, intent);
    }

}
