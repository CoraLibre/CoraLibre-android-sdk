package org.coralibre.android.sdk.internal.util

object ByteToHex {
    @JvmStatic
    fun toString(data: ByteArray): String {
        val builder = StringBuilder()
        for (b in data) {
            builder.append(String.format("%02X", b))
        }
        return builder.toString()
    }
}
