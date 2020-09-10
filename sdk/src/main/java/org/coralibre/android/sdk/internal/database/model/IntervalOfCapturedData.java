package org.coralibre.android.sdk.internal.database.model;

import org.coralibre.android.sdk.internal.crypto.ENNumber;

import java.util.List;

public interface IntervalOfCapturedData {

    ENNumber getInterval();

    List<CapturedData> getCapturedData();

    void add(CapturedData capturedData);

}
