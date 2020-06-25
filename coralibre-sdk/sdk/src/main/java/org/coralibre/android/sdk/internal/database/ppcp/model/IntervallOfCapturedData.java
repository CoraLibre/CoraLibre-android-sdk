package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

import java.util.List;

public interface IntervallOfCapturedData {

    ENNumber getInterval();

    List<CapturedData> getCapturedData();

    void add(CapturedData capturedData);

}
