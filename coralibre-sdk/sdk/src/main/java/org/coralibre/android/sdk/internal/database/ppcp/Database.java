package org.coralibre.android.sdk.internal.database.ppcp;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.ppcp.models.Exposure;

import java.util.Set;

public interface Database {
    void addExposure(Exposure exposure);

    Set<Exposure> getExposuresBySlot(ENNumber enNumber);
}
