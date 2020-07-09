package org.coralibre.android.sdk.internal.util;

public class ByteToHex {
    public static String toString(byte[] data) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : data) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
}
