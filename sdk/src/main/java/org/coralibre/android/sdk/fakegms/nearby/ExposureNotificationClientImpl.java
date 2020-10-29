package org.coralibre.android.sdk.fakegms.nearby;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.coralibre.android.sdk.PPCP;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureNotificationClient;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey;
import org.coralibre.android.sdk.fakegms.tasks.Task;
import org.coralibre.android.sdk.fakegms.tasks.Tasks;
import org.coralibre.android.sdk.internal.EnFrameworkConstants;
import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.database.DatabaseAccess;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey;
import org.coralibre.android.sdk.internal.datatypes.util.DiagnosisKeyUtil;
import org.coralibre.android.sdk.internal.matching.MatchingLegacyV1;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyExport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


// TODO What happens with queries for tokens, for that no keys have been provided?
// TODO When are tokens with data deleted?

final class ExposureNotificationClientImpl implements ExposureNotificationClient {
    public static final String TAG = ExposureNotificationClientImpl.class.getSimpleName();

    private final Context context;
    private Database database;


    ExposureNotificationClientImpl(@NonNull final Context context) {
        this.context = context;
        // TODO the initialization should be called asynchronously
        PPCP.init(context);
    }

    private boolean isPPCPEnabled() {
        return PPCP.isStarted(context);
    }

    @Override
    public Task<Void> start() {
        return Tasks.call(() -> {
            if (!isPPCPEnabled()) {
                PPCP.start(context);

                // TODO: Change after refactoring database creation / factory
                //database = new PersistentDatabase(context);
                database = DatabaseAccess.getDefaultDatabaseInstance();
            }
            return null;
        });
    }

    @Override
    public Task<Void> stop() {
        return Tasks.call(() -> {
            if (isPPCPEnabled()) {
                PPCP.stop(context);
                database = null;
            }
            return null;
        });
    }

    @Override
    public Task<Boolean> isEnabled() {
        return Tasks.forResult(isPPCPEnabled());
    }

    /**
     * @return the user's TEKs from the last 14 days
     */
    @Override
    public Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory() {
        return Tasks.call(() -> {
            Iterable<InternalTemporaryExposureKey> dbTeks = database.getAllOwnTEKs();
            List<TemporaryExposureKey> result = new LinkedList<TemporaryExposureKey>();
            for (InternalTemporaryExposureKey dbTek : dbTeks) {
                result.add(new TemporaryExposureKey(
                    dbTek.getKey(),
                    (int) dbTek.getInterval().get(),
                    EnFrameworkConstants.TEK_ROLLING_PERIOD,
                    0, // TODO Means "Unused"; is this correct here? verify, that the CWA sets this value before uploading
                    0 // TODO this means "UNKNOWN"; is this correct here? see https://developers.google.com/android/exposure-notifications/exposure-notifications-api
                ));
            }
            return result;
        });
    }

    @Override
    public Task<Void> provideDiagnosisKeys(final List<File> keyFiles,
                                           @Nullable final ExposureConfiguration exposureConfiguration,
                                           final String token) {
        return Tasks.call(() -> {

            if (exposureConfiguration == null || token == null || token.isEmpty()) {
                throw new IllegalArgumentException("EN framework: Invalid parameter for 'provideDiagnosisKeys(...)'.");
            }

            // TODO save exposure configuration to database, to restore it after e.g. phone restart, like microg?

            for (File file : keyFiles) {
                try (ZipInputStream stream = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {

                    ZipEntry zipEntry;
                    while ((zipEntry = stream.getNextEntry()) != null) {
                        if (zipEntry.getName().equals("export.bin")) {
                            byte[] prefix = new byte[16];
                            int totalBytesRead = 0;
                            int bytesRead = 0;

                            while (bytesRead != -1 && totalBytesRead < prefix.length) {
                                bytesRead = stream.read(prefix, totalBytesRead, prefix.length - totalBytesRead);
                                if (bytesRead > 0) {
                                    totalBytesRead += bytesRead;
                                }
                            }

                            String prefixString = new String(prefix).trim();
                            if (totalBytesRead == prefix.length && prefixString.equals("EK Export v1")) {
                                final TemporaryExposureKeyExport temporaryExposureKeyExport = TemporaryExposureKeyExport.parseFrom(stream);
                                database.addDiagnosisKeys(token, DiagnosisKeyUtil.toDiagnosisKeys(temporaryExposureKeyExport.getKeysList()));
                                database.updateDiagnosisKeys(token, DiagnosisKeyUtil.toDiagnosisKeys(temporaryExposureKeyExport.getRevisedKeysList()));
                                // TODO verify that first add and then update is correct here
                            } else {
                                Log.e(TAG, "Failed to parse diagnosis key file: export.bin has invalid prefix: " + prefixString);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse diagnosis key file", e);
                }
            }


            // TODO Discard keys older than 14 days
            //  See description of method "provideDiagnosisKeys()" in:
            //  https://developers.google.com/android/exposure-notifications/exposure-notifications-api
            //  "Keys provided with the same token accumulate into the same set, and are aged out
            //  of those sets as they pass out of the 14-day window."

            // TODO Are measurements from today used for the matching? If not, remove them before
            //  testing for matches (same for the ExposureSummary/ExposureInformation computation)

            List<DiagnosisKey> diagnosisKeys = database.getDiagnosisKeys(token);
            Iterable<IntervalOfCapturedData> capturedData = database.getAllCollectedPayload();
            boolean noMatchFound = !MatchingLegacyV1.hasMatches(diagnosisKeys, capturedData);

            Intent intent = new Intent(noMatchFound ? ACTION_EXPOSURE_NOT_FOUND : ACTION_EXPOSURE_STATE_UPDATED);
            intent.putExtra(EXTRA_TOKEN, token);
            context.sendOrderedBroadcast(intent, null);
            return null;
        });
    }

    @Override
    public Task<ExposureSummary> getExposureSummary(String token) {
        return Tasks.call(() -> {

            // TODO use MatchingLegacyV1 to get ExposureSummary item

            return null;
        });
    }

    @Override
    public Task<List<ExposureInformation>> getExposureInformation(String token) {
        return Tasks.call(() -> {
            // See src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt
            List<ExposureInformation> result = new ArrayList<>();

            // TODO use MatchingLegacyV1 to get ExposureInformation items

            return result;
        });
    }
}
