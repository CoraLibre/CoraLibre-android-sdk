package org.coralibre.android.sdk.internal.crypto.ppcp;

public class CryptoException extends RuntimeException {
    CryptoException(Throwable t) {
        super(t);
    }

    CryptoException(String message) {
        super(message);
    }

    CryptoException(String message, Throwable t) {
        super(message, t);
    }
}
