package org.coralibre.android.sdk.internal.datatypes

import java.util.ArrayList

class IntervalOfCapturedData(val interval: ENInterval) {
    private val capturedData: MutableList<CapturedData> = ArrayList()
    fun add(capturedData: CapturedData) {
        this.capturedData.add(capturedData)
    }

    fun getCapturedData(): List<CapturedData> {
        return capturedData
    }
}
