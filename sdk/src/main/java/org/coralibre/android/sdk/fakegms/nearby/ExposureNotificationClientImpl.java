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
import org.coralibre.android.sdk.internal.datatypes.TemporaryExposureKey_internal;
import org.coralibre.android.sdk.internal.datatypes.util.DiagnosisKeyUtil;
import org.coralibre.android.sdk.internal.matching.IdentifyMatchesFromDb;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyExport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class ExposureNotificationClientImpl implements ExposureNotificationClient {
    public static final String TAG = ExposureNotificationClientImpl.class.getSimpleName();
    private static Nearby instance = null;

    private final Context context;
    private boolean enabled = false;
    private Database database;

    private ExposureConfiguration exposureConfiguration;

    ExposureNotificationClientImpl(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public Task<Void> start() {
        return Tasks.call(() -> {
            if (!enabled) {
                PPCP.start(context);

                // TODO: Change after refactoring database creation / factory
                //database = new PersistentDatabase(context);
                database = DatabaseAccess.getDefaultDatabaseInstance();

                enabled = true;
            }
            return null;
        });
    }

    @Override
    public Task<Void> stop() {
        return Tasks.call(() -> {
            if (enabled) {
                PPCP.stop(context);
                database = null;
                enabled = false;
            }
            return null;
        });
    }

    @Override
    public Task<Boolean> isEnabled() {
        return Tasks.forResult(enabled);
    }

    /**
     * @return the user's TEKs from the last 14 days
     */
    @Override
    public Task<List<TemporaryExposureKey>> getTemporaryExposureKeyHistory() {
        return Tasks.call(() -> {
            Iterable<TemporaryExposureKey_internal> dbTeks = database.getAllOwnTEKs();
            List<TemporaryExposureKey> result = new LinkedList<TemporaryExposureKey>();
            for (TemporaryExposureKey_internal dbTek : dbTeks) {
                result.add(new TemporaryExposureKey(
                    dbTek.getKey(),
                    (int) dbTek.getInterval().get(),
                    EnFrameworkConstants.TEK_ROLLING_PERIOD,
                    0, // TODO Means "Unused"; verify, that the CWA sets this value before uploading
                    0 // TODO this means "UNKNOWN"; see https://developers.google.com/android/exposure-notifications/exposure-notifications-api
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
            // TODO save exposure configuration to database, to restore it after e.g. phone restart, like microg?
            this.exposureConfiguration = exposureConfiguration;

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
                            } else {
                                Log.e(TAG, "Failed to parse diagnosis key file: export.bin has invalid prefix: " + prefixString);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse diagnosis key file", e);
                }
            }


            // TODO Discard keys older than 14 days (https://developers.google.com/android/reference/com/google/android/gms/nearby/exposurenotification/ExposureNotificationClient#provideDiagnosisKeys(com.google.android.gms.nearby.exposurenotification.DiagnosisKeyFileProvider))

            boolean noMatchFound = !IdentifyMatchesFromDb.hasMatches(token);

            Intent intent = new Intent(noMatchFound ? ACTION_EXPOSURE_NOT_FOUND : ACTION_EXPOSURE_STATE_UPDATED);
            intent.putExtra(EXTRA_TOKEN, token);
            context.sendOrderedBroadcast(intent, null);
            return null;
        });
    }

    @Override
    public Task<ExposureSummary> getExposureSummary(String token) {
        return Tasks.call(() -> {
            // TODO Here it is unclear, when the exposureConfiguration can be null. Also it is,
            //  unclear, if different exposureConfigurations can be around, and if so, which to use.
            //  Should the exposureConfiguration also be selected using the token? If so, remove
            //  the exposure configuration parameter from the following call and implement the
            //  selection inside the buildExposureSummaryFromMatches(...) method, using the database
            //  in a similar manner as already done for the diagnosis key sets.
            return IdentifyMatchesFromDb.buildExposureSummary(token, exposureConfiguration);
        });
    }

    @Override
    public Task<List<ExposureInformation>> getExposureInformation(String token) {
        return Tasks.call(() -> {
            // See src/deviceForTesters/java/de.rki.coronawarnapp/TestRiskLevelCalculation.kt
            List<ExposureInformation> result = new ArrayList<>();

            // TODO loop through matched keys and build exposure information items

            return result;
        });
    }
}
