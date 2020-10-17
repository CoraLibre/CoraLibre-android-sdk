package org.coralibre.android.sdk.internal.matching;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;

import java.util.List;

public class AllExposureInfo {

    public final List<ExposureInformation> exposureInformationList;
    public final ExposureSummary exposureSummary;


    public AllExposureInfo(
        List<ExposureInformation> exposureInformationList,
        ExposureSummary exposureSummary
    ) {
        this.exposureInformationList = exposureInformationList;
        this.exposureSummary = exposureSummary;
    }
}
