package org.coralibre.android.sdk.internal.database.model;

import org.coralibre.android.sdk.internal.crypto.ENInterval;

import java.util.List;

public interface IntervalOfCapturedData {

    ENInterval getInterval();

    List<CapturedData> getCapturedData();

    void add(CapturedData capturedData);

}
