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

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.security.SecureRandom
import java.util.ArrayList
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RunWith(Parameterized::class)
class AESBenchmark(private val size: Int) {
    @Rule
    @JvmField
    val benchmarkRule = BenchmarkRule()

    @Test
    fun microCreateEphIDs() {
        val state = benchmarkRule.getState()
        val secretKey = ByteArray(32)
        SecureRandom().nextBytes(secretKey)
        val broadcastKey = "BLUBBLUBBLUBBLUBBLUBBLUBBLUBBLUB".toByteArray()
        // TODO: this method of timing does not make for a very good benchmark.
        while (state.keepRunning()) {
            val start = System.currentTimeMillis()
            createEphIDs(secretKey, broadcastKey, size)
            val end = System.currentTimeMillis()
            Log.d(
                "AESBenchmark",
                String.format("Creating $size ephIDs took %.3f seconds.", (end - start) / 1000.0)
            )
        }
    }

    private fun createEphIDs(
        secretKey: ByteArray,
        broadcastKey: ByteArray,
        epochs: Int
    ): ArrayList<ByteArray> {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        mac.update(broadcastKey)
        val keyBytes = mac.doFinal()
        val emptyArray = ByteArray(16)

        // generate EphIDs
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, SecureRandom())
        val ephIds = ArrayList<ByteArray>()
        for (i in 0 until epochs) {
            ephIds.add(cipher.update(emptyArray))
        }
        return ephIds
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Int> {
            return listOf(100, 1000, 10000, 100000)
        }
    }
}
