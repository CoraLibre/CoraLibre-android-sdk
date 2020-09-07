package org.coralibre.android.sdk.fakegms.nearby;

import androidx.annotation.Nullable;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureNotificationClient;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey;
import org.coralibre.android.sdk.fakegms.tasks.Task;
import org.coralibre.android.sdk.fakegms.tasks.TaskAutorunOnceListenersThere;

import java.io.File;
import java.util.List;

final class ExposureNotificationClientImpl implements ExposureNotificationClient {

    @Override
    public Task<Void> start() {
        return new TaskAutorunOnceListenersThere<Void>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    @Override
    public Task<Void> stop() {
        return new TaskAutorunOnceListenersThere<Void>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    @Override
    public Task<Boolean> isEnabled() {
        return new TaskAutorunOnceListenersThere<Boolean>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    @Override

    public Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory() {
        return new TaskAutorunOnceListenersThere<List<TemporaryExposureKey>>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    @Override
    public Task<Void> provideDiagnosisKeys(final List<File> keyFiles,
                                           @Nullable final ExposureConfiguration exposureConfiguration,
                                           final String token) {
        return new TaskAutorunOnceListenersThere<Void>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    @Override
    public Task<ExposureSummary> getExposureSummary(String token) {
        return new TaskAutorunOnceListenersThere<ExposureSummary>() {
            @Override
            public void runInternal() {
                // TODO: Implement!
            }
        };
    }

    @Override
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
