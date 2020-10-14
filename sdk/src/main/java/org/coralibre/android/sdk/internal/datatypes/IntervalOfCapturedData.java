package org.coralibre.android.sdk.internal.datatypes;

import java.util.List;

public interface IntervalOfCapturedData {

    ENInterval getInterval();

    List<CapturedData> getCapturedData();

    void add(final CapturedData capturedData);

}
