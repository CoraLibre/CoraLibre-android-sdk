package org.coralibre.android.sdk.internal.database.ppcp.models;

import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

import java.util.Arrays;
import java.util.Objects;

public class Exposure {

    private final ENNumber enNumber;

    private byte[] payload;

    Exposure() {
        this.enNumber = CryptoModule.getCurrentENNumber();
    }

    public ENNumber getEnNumber() {
        return enNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exposure exposure = (Exposure) o;
        return Objects.equals(enNumber, exposure.enNumber) &&
                Arrays.equals(payload, exposure.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(enNumber);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
