package org.coralibre.android.sdk.internal.database.model;

import org.coralibre.android.sdk.internal.crypto.ENInterval;

import java.util.ArrayList;
import java.util.List;

public class IntervalOfCapturedDataImpl implements IntervalOfCapturedData {

    ENInterval interval;
    List<CapturedData> capturedData = new ArrayList<>();

    public IntervalOfCapturedDataImpl(ENInterval interval) {
        this.interval = interval;
    }

    public void add(CapturedData capturedData) {
        this.capturedData.add(capturedData);
    }

    @Override
    public ENInterval getInterval() {
        return interval;
    }

    public List<CapturedData> getCapturedData() {
        return capturedData;
    }
}
