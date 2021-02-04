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

import android.content.Context
import android.content.Intent

object BroadcastHelper {
    const val ACTION_UPDATE_ERRORS = "org.coralibre.android.sdk.internal.ACTION_UPDATE_ERRORS"

    fun sendUpdateBroadcast(context: Context) {
        val intent = Intent(CoraLibre.UPDATE_INTENT_ACTION)
        context.sendBroadcast(intent)
    }

    @JvmStatic
    fun sendErrorUpdateBroadcast(context: Context) {
        sendUpdateBroadcast(context)
        val intent = Intent(ACTION_UPDATE_ERRORS)
        context.sendBroadcast(intent)
    }
}
