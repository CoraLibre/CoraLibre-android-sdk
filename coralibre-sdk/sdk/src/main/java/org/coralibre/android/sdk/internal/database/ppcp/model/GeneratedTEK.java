package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

public interface GeneratedTEK {
    ENNumber getInterval();

    byte[] getKey();
}
