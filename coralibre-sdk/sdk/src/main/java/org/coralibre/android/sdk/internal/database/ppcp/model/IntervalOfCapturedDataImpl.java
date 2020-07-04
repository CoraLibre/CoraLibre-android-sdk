package org.coralibre.android.sdk.internal.database.ppcp.model;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

import java.util.ArrayList;
import java.util.List;

public class IntervalOfCapturedDataImpl implements IntervalOfCapturedData {

    ENNumber interval;
    List<CapturedData> capturedData = new ArrayList<>();

    public IntervalOfCapturedDataImpl(ENNumber interval) {
        this.interval = interval;
    }

    public void add(CapturedData capturedData) {
        this.capturedData.add(capturedData);
    }

    @Override
    public ENNumber getInterval() {
        return interval;
    }

    public List<CapturedData> getCapturedData() {
        return capturedData;
    }
}
