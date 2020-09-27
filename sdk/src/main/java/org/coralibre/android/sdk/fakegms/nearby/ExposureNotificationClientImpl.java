package org.coralibre.android.sdk.fakegms.nearby;

import android.content.Context;
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
import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.database.PersistentDatabase;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyExport;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyProto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                database = new PersistentDatabase(context);
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
            List<TemporaryExposureKey> result = new ArrayList<>();
            for (IntervalOfCapturedData intervalOfCapturedData : database.getAllCollectedPayload()) {
                CapturedData firstCapturedData = intervalOfCapturedData.getCapturedData().get(0);

                result.add(new TemporaryExposureKey.TemporaryExposureKeyBuilder()
                    .setKeyData(firstCapturedData.getPayload()) // TODO is this the correct key?
                    .setRollingStartIntervalNumber((int) firstCapturedData.getEnInterval().get())
                    .setRollingPeriod((int) intervalOfCapturedData.getInterval().get()) // number of 10-minutes intervals covered
                    .build());

                //.setTransmissionRiskLevel() not used because how could we know what
                // transmission risk level would be assigned to this key by the library user?
                // not even the microg's implementation does it
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
                                database.addDiagnosisKeys(temporaryExposureKeyExport.getKeysList());
                                database.updateDiagnosisKeys(temporaryExposureKeyExport.getRevisedKeysList());
                            } else {
                                Log.e(TAG, "Failed to parse diagnosis key file: export.bin has invalid prefix: " + prefixString);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse diagnosis key file", e);
                }
            }

            // TODO match provided keys with database
            // TODO send intent to notify of exposure found / not found

            return null;
        });
    }

    @Override
    public Task<ExposureSummary> getExposureSummary(String token) {
        return Tasks.call(() -> {
            long minimumCaptureTimestamp = Long.MAX_VALUE;
            int maximumRiskScore = 0;
            int summationRiskScore = 0;
            final Set<byte[]> distinctKeys = new HashSet<>();

            // TODO loop through matched keys and collect the above data

            return new ExposureSummary.ExposureSummaryBuilder()
                .setDaysSinceLastExposure((int) minimumCaptureTimestamp) // TODO calculate days
                .setMaximumRiskScore(maximumRiskScore)
                .setSummationRiskScore(summationRiskScore)
                .setMatchedKeyCount(distinctKeys.size())
                .setAttenuationDurations(new int[3]) // TODO
                .build();
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
