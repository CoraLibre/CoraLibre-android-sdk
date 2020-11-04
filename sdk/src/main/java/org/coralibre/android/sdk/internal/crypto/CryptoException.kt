package org.coralibre.android.sdk.internal.crypto

class CryptoException : RuntimeException {
    internal constructor(t: Throwable?) : super(t)
    internal constructor(message: String?) : super(message)
    internal constructor(message: String?, t: Throwable?) : super(message, t)
}
