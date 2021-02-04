package org.coralibre.android.sdk.fakegms.nearby;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureNotificationClient;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey;
import org.coralibre.android.sdk.fakegms.tasks.Task;
import org.coralibre.android.sdk.fakegms.tasks.Tasks;
import org.coralibre.android.sdk.internal.CoraLibre;
import org.coralibre.android.sdk.internal.util.IoExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


// TODO What happens with queries for tokens, for that no keys have been provided?
// TODO When are tokens with data deleted?

final class ExposureNotificationClientImpl implements ExposureNotificationClient {
    public static final String TAG = ExposureNotificationClientImpl.class.getSimpleName();

    private final Context context;

    ExposureNotificationClientImpl(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public Task<Void> start() {
        return Tasks.call(IoExecutor.INSTANCE, () -> {
            if (!CoraLibre.isEnabled(context)) {
                Log.d(TAG, "Starting...");
                CoraLibre.enable(context);
            }
            return null;
        });
    }

    @Override
    public Task<Void> stop() {
        return Tasks.call(IoExecutor.INSTANCE, () -> {
            if (CoraLibre.isEnabled(context)) {
                Log.d(TAG, "Stopping...");
                CoraLibre.stop(context);
            }
            return null;
        });
    }

    @Override
    public Task<Boolean> isEnabled() {
        return Tasks.call(() -> CoraLibre.isEnabled(context));
    }

    /**
     * @return the user's TEKs from the last 14 days
     */
    @Override
    public Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory() {
        return Tasks.call(
            IoExecutor.INSTANCE,
            () -> CoraLibre.getTemporaryExposureKeyHistory(context)
        );
    }

    @Override
    public Task<Void> provideDiagnosisKeys(
        final List<File> keyFiles,
        @Nullable final ExposureConfiguration exposureConfiguration,
        final String token
    ) {
        return Tasks.call(IoExecutor.INSTANCE, () -> {
            CoraLibre.provideDiagnosisKeys(context, keyFiles, exposureConfiguration, token);
            return null;
        });
    }

    @Override
    public Task<ExposureSummary> getExposureSummary(String token) {
        return Tasks.call(IoExecutor.INSTANCE, () -> {

            // TODO use MatchingLegacyV1 to get ExposureSummary item

            return new ExposureSummary.ExposureSummaryBuilder()
                .setAttenuationDurations(new int[]{0, 0, 0})
                .setMatchedKeyCount(0)
                .setMaximumRiskScore(0)
                .setSummationRiskScore(0)
                .build();
        });
    }

    @Override
    public Task<List<ExposureInformation>> getExposureInformation(String token) {
        return Tasks.call(IoExecutor.INSTANCE, () -> {
            // See src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt
            List<ExposureInformation> result = new ArrayList<>();

            // TODO use MatchingLegacyV1 to get ExposureInformation items

            return result;
        });
    }
}
