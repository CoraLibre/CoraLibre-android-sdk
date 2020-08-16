package org.coralibre.android.sdk.fakegms.nearby;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey;
import org.coralibre.android.sdk.fakegms.tasks.Task;
import org.coralibre.android.sdk.fakegms.tasks.TaskAutorunOnceListenersThere;

import java.io.File;
import java.util.List;


public class Nearby {

    //
    // The gms Nearby is used in particular by the following classes (and perhaps more):
    //
    //  InternalExposureNotificationClient
    //  https://github.com/corona-warn-app/cwa-app-android/blob/master/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/nearby/InternalExposureNotificationClient.kt
    //


    private static Nearby instance = null;


    public static Nearby getExposureNotificationClient(@NonNull Object context) {
        // This strange type name mismatching is expected by the rki app.

        if (instance == null) {
            instance = new Nearby();
        }
        return instance;
    }



    /**
     * The returned Task object's addOnSuccessListener(...) AND addOnFailureListener(...)
     * both MUST be called in order for the start() call to work as expected. Otherwise,
     * the task wont be started.
     * This is a workaround to be able to ensure, that the listeners have actually been set before
     * the internal task action starts, which is required to ensure that the listeners have been
     * set before the task action finishes. Also see the TaskAutostartEnService class.
     */
    public Task<Void> start() {
        return new TaskAutorunOnceListenersThere<Void>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }


    // TODO: Implement the following methods.

    public Task<Void> stop() {
        return new TaskAutorunOnceListenersThere<Void>() {
            @Override
            public void runInternal() {
            // TODO: Implement!
            }
        };
    }

    public Task<Boolean> isEnabled() {
        return new TaskAutorunOnceListenersThere<Boolean>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    public Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory() {
        return new TaskAutorunOnceListenersThere<List<TemporaryExposureKey>>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }


    /**
     * Adds keys of infected people contained in {@code keyFiles} to the database and
     * provides to the client the {@code exposureConfiguration} to use to calculate risk
     *
     * @see <a href="https://developers.google.com/android/exposure-notifications/exposure-notifications-api#providediagnosiskeys">description on developers.google.com</a>
     * @param keyFiles a list of files to extract the keys of confirmed cases from, obtained from
     *                 an internet-accessible server. The file format is described
     *                 <a href="https://developers.google.com/android/exposure-notifications/exposure-key-file-format">on developers.google.com</a>
     *                 [TODO still unused]
     * @param exposureConfiguration the configuration to use to calculate risk exposure, needs to
     *                              be provided before calling {@link #getExposureSummary(String)}
     *                              and {@link #getExposureInformation(String)}
     * @param token identifier that could be randomly generated for every call or reused to group
     *              together results in calls to {@link #getExposureSummary(String)}
     *              and {@link #getExposureInformation(String)} [TODO still unused]
     * @return a runnable task
     */
    public Task<Void> provideDiagnosisKeys(final List<File> keyFiles,
            @Nullable final ExposureConfiguration exposureConfiguration, final String token) {
        return new TaskAutorunOnceListenersThere<Void>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    public Task<ExposureSummary> getExposureSummary(String token) {
        return new TaskAutorunOnceListenersThere<ExposureSummary>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }


    public Task<List<ExposureInformation>> getExposureInformation(String token) {
        return new TaskAutorunOnceListenersThere<List<ExposureInformation>>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
                // See src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt
            }
        };
    }

}
