package org.coralibre.android.sdk.internal.database.ppcp.models;

import org.coralibre.android.sdk.internal.crypto.ppcp.BluetoothPayload;
import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

import java.util.Arrays;
import java.util.Objects;

public class CollectedPayload {

    private BluetoothPayload data;

    CollectedPayload(BluetoothPayload payload) {
        data = payload;
    }

    public ENNumber getEnNumber() {
        return data.getInterval();
    }

    public byte[] getRawData() {
        return data.getRawPayload();
    }

    public BluetoothPayload getData() {
        return data;
    }
}
