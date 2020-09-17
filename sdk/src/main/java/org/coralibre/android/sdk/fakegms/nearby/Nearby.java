package org.coralibre.android.sdk.fakegms.nearby;

import android.content.Context;

import androidx.annotation.NonNull;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureNotificationClient;


public final class Nearby {

    public static ExposureNotificationClient getExposureNotificationClient(@NonNull Context context) {
        return new ExposureNotificationClientImpl();
    }
}
