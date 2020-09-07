package org.coralibre.android.sdk.fakegms.nearby.exposurenotification;

import androidx.annotation.Nullable;

import org.coralibre.android.sdk.fakegms.tasks.Task;

import java.io.File;
import java.util.List;

public interface ExposureNotificationClient {

    //
    // The gms ExposureNotificationClient is used in particular by the following classes (and perhaps more):
    //
    //  ExposureStateUpdateWorker
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt
    //

    // TODO: Used in the ExposureStateUpdateWorker.kt, but couldn't find documentation yet.
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/ExposureStateUpdateWorker.kt
    //  Also see:
    //  src/test/java/de/rki/coronawarnapp/receiver/ExposureStateUpdateReceiverTest.kt
    public static String EXTRA_TOKEN = "-1";

    // See:
    // src/deviceForTesters/java/de.rki.coronawarnapp/TestForAPIFragment.kt
    public static String ACTION_EXPOSURE_STATE_UPDATED
        = "org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ACTION_EXPOSURE_STATE_UPDATED";

    /**
     * The returned Task object's addOnSuccessListener(...) AND addOnFailureListener(...)
     * both MUST be called in order for the start() call to work as expected. Otherwise,
     * the task wont be started.
     * This is a workaround to be able to ensure, that the listeners have actually been set before
     * the internal task action starts, which is required to ensure that the listeners have been
     * set before the task action finishes. Also see the TaskAutostartEnService class.
     */
    Task<Void> start();

    Task<Void> stop();

    Task<Boolean> isEnabled();

    Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory();

    /**
     * Adds keys of infected people contained in {@code keyFiles} to the database and
     * provides to the client the {@code exposureConfiguration} to use to calculate risk
     *
     * @param keyFiles              a list of files to extract the keys of confirmed cases from, obtained from
     *                              an internet-accessible server. The file format is described
     *                              <a href="https://developers.google.com/android/exposure-notifications/exposure-key-file-format">on developers.google.com</a>
     *                              [TODO still unused]
     * @param exposureConfiguration the configuration to use to calculate risk exposure, needs to
     *                              be provided before calling {@link #getExposureSummary(String)}
     *                              and {@link #getExposureInformation(String)}
     * @param token                 identifier that could be randomly generated for every call or reused to group
     *                              together results in calls to {@link #getExposureSummary(String)}
     *                              and {@link #getExposureInformation(String)} [TODO still unused]
     * @return a runnable task
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#providediagnosiskeys">description on developers.google.com</a>
     */
    Task<Void> provideDiagnosisKeys(final List<File> keyFiles,
                                    @Nullable final ExposureConfiguration exposureConfiguration,
                                    final String token);

    Task<ExposureSummary> getExposureSummary(String token);

    Task<List<ExposureInformation>> getExposureInformation(String token);
}
