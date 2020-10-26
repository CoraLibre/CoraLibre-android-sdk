package org.coralibre.android.sdk.internal.datatypes;

import java.util.ArrayList;
import java.util.List;

public class IntervalOfCapturedData {

    private final ENInterval interval;
    private final List<CapturedData> capturedData = new ArrayList<>();


    public IntervalOfCapturedData(ENInterval interval) {
        this.interval = interval;
    }

    public void add(final CapturedData capturedData) {
        this.capturedData.add(capturedData);
    }


    public ENInterval getInterval() {
        return interval;
    }

    public List<CapturedData> getCapturedData() {
        return capturedData;
    }
}
