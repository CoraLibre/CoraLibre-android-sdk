package org.coralibre.android.sdk.fakegms.nearby;

import androidx.annotation.Nullable;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureNotificationClient;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey;
import org.coralibre.android.sdk.fakegms.tasks.Task;
import org.coralibre.android.sdk.fakegms.tasks.Tasks;

import java.io.File;
import java.util.Collections;
import java.util.List;

final class ExposureNotificationClientImpl implements ExposureNotificationClient {

    @Override
    public Task<Void> start() {
        // TODO: Implement!
        return Tasks.forResult(null);
    }

    @Override
    public Task<Void> stop() {
        // TODO: Implement!
        return Tasks.forResult(null);
    }

    @Override
    public Task<Boolean> isEnabled() {
        // TODO: Implement!
        return Tasks.forResult(true);
    }

    @Override
    public Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory() {
        // TODO: Implement!
        return Tasks.forResult(Collections.emptyList());
    }

    @Override
    public Task<Void> provideDiagnosisKeys(final List<File> keyFiles,
                                           @Nullable final ExposureConfiguration exposureConfiguration,
                                           final String token) {
        // TODO: Implement!
        return Tasks.forResult(null);
    }

    @Override
    public Task<ExposureSummary> getExposureSummary(String token) {
        return Tasks.forResult(new ExposureSummary.ExposureSummaryBuilder()
            .setAttenuationDurations(new int[]{0, 0, 0})
            .setMatchedKeyCount(0)
            .setMaximumRiskScore(0)
            .setSummationRiskScore(0)
            .build());
    }

    @Override
    public Task<List<ExposureInformation>> getExposureInformation(String token) {
        // TODO: Implement!
        // See src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt
        return Tasks.forResult(Collections.emptyList());
    }
}
