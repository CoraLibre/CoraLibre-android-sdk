package org.coralibre.android.sdk.internal.matching

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary

class AllExposureInfo(
    val exposureInformationList: List<ExposureInformation>,
    val exposureSummary: ExposureSummary
)
