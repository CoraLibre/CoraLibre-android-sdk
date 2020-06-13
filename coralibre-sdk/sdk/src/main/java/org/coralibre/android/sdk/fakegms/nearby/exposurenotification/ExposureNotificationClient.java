package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import android.content.Context;

import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;

import java.util.List;

public class ExposureNotificationClient {

    //
    // The gms ExposureNotificationClient is used in particular by the following classes (and perhaps more):
    //
    //  ExposureStateUpdateWorker
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt
    //


    public static String EXTRA_TOKEN = "-1";
    // TODO: Used in the ExposureStateUpdateWorker.kt, but couldn't find documentation yet.
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt
    //  Also see:
    //  src/test/java/de/rki/coronawarnapp/receiver/ExposureStateUpdateReceiverTest.kt

    public static String ACTION_EXPOSURE_STATE_UPDATED
            = "org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ACTION_EXPOSURE_STATE_UPDATED";
        // See:
        // src/deviceForTesters/java/de.rki.coronawarnapp/TestForAPIFragment.kt

}
