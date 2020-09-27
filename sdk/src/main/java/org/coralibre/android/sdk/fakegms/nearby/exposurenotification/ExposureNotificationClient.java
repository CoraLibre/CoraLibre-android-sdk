package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import androidx.annotation.Nullable;

import org.coralibre.android.sdk.fakegms.tasks.Task;

import java.io.File;
import java.util.List;

public interface ExposureNotificationClient {

    // The gms ExposureNotificationClient is used in particular by the following classes (and perhaps more):
    //
    //  ExposureStateUpdateWorker
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt


    // See Corona-Warn-App usages for the values below:
    // https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/deviceForTesters/java/de.rki.coronawarnapp/TestForAPIFragment.kt
    // https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt
    // https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/test/java/de/rki/coronawarnapp/receiver/ExposureStateUpdateReceiverTest.kt
    // See microg values:
    // https://github.com/microg/android_packages_apps_GmsCore/blob/master/play-services-nearby-api/src/main/java/org/microg/gms/nearby/exposurenotification/Constants.java
    String ACTION_EXPOSURE_NOT_FOUND = "com.google.android.gms.exposurenotification.ACTION_EXPOSURE_NOT_FOUND";
    String ACTION_EXPOSURE_STATE_UPDATED = "org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ACTION_EXPOSURE_STATE_UPDATED";
    String EXTRA_TOKEN = "com.google.android.gms.exposurenotification.EXTRA_TOKEN";

    Task<Void> start();

    Task<Void> stop();

    Task<Boolean> isEnabled();

    Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory();

    /**
     * Adds keys of infected people contained in {@code keyFiles} to the database and provides to
     * the client the {@code exposureConfiguration} to use to calculate risk. At the end of the
     * execution fires an {@link android.content.Intent} with action {@link #ACTION_EXPOSURE_STATE_UPDATED}
     * if a match was found, and action {@link #ACTION_EXPOSURE_NOT_FOUND} otherwise, with an extra
     * at {@link #EXTRA_TOKEN} containing the provided {@code token}.
     *
     * @param keyFiles a list of files to extract the keys of confirmed cases from, obtained from an
     *                 internet-accessible server. The file format is described
     *                 <a href="https://developers.google.com/android/exposure-notifications/exposure-key-file-format">on developers.google.com</a>
     * @param exposureConfiguration the configuration to use to calculate risk exposure, needs to
     *                              be provided before calling {@link #getExposureSummary(String)}
     *                              and {@link #getExposureInformation(String)}
     * @param token identifier that could be randomly generated for every call, used just for the
     *              intent (the original gms implementation would use it also to put limitations on
     *              the number of calls, but this is not done here).
     * @return a runnable task
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#providediagnosiskeys">description on developers.google.com</a>
     */
    Task<Void> provideDiagnosisKeys(final List<File> keyFiles,
                                    @Nullable final ExposureConfiguration exposureConfiguration,
                                    final String token);

    Task<ExposureSummary> getExposureSummary(String token);

    Task<List<ExposureInformation>> getExposureInformation(String token);
}
