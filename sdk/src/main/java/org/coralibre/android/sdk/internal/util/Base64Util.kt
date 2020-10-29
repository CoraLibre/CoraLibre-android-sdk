/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.coralibre.android.sdk.internal.util

import android.util.Base64

object Base64Util {
    @JvmStatic
    fun toBase64(data: ByteArray): String {
        return String(Base64.encode(data, Base64.NO_WRAP))
    }

    @JvmStatic
    fun fromBase64(data: String): ByteArray {
        return Base64.decode(data, Base64.NO_WRAP)
    }
}
