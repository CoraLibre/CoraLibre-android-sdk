package org.coralibre.android.sdk.internal.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.internal.EnFrameworkConstants;
import org.coralibre.android.sdk.internal.database.persistent.RoomDatabaseDelegate;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureInformation;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureSummary;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey;
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PersistentDatabase implements Database {

    // TODO: Since we're storing very sensitive data here, we should probably encrypt the database
    //  using SQLCipher


    private final String dbName = "coralibre-en-database";
    private final RoomDatabaseDelegate db;


    /**
     * Creates a persistent database (equivalent to calling the other constructor with inMemory = false).
     * @param context   The context for the database. This is usually the Application context.
     */
    public PersistentDatabase(@NonNull Context context) {
        this(context, false);
    }


    /**
     * @param context   The context for the database. This is usually the Application context.
     * @param inMemory  If true, an inMemoryDatabaseBuilder is used for creating, resulting in stored data disappearing when the process is killed. Otherwise, a databaseBuilder is used, for aan actually persistent database.
     */
    public PersistentDatabase(@NonNull Context context, boolean inMemory) {
        if (inMemory) {
            db = Room.inMemoryDatabaseBuilder(context, RoomDatabaseDelegate.class).build();
        } else {
            db = Room.databaseBuilder(context, RoomDatabaseDelegate.class, dbName).build();
        }
    }


    @Override
    public void addGeneratedTEK(InternalTemporaryExposureKey generatedTEK) {
        db.daoTEK().insertTEK(new EntityTemporaryExposureKey(generatedTEK));
    }

    @Override
    public void addCapturedPayload(CapturedData collectedPayload) {
        db.daoCapturedData().insertCapturedData(new EntityCapturedData(collectedPayload));
    }



    @Override
    public void addDiagnosisKeys(String token, List<DiagnosisKey> diagnosisKeys) {
        EntityToken entityToken = db.daoToken().getToken(token);
        if (entityToken == null) {
            db.daoToken().insertToken(new EntityToken(token, false));
        } else {
            entityToken.exposureDataUpToDate = false;
            db.daoToken().updateToken(entityToken);
        }
        db.daoDiagnosisKey().insertDiagnosisKeys(
            EntityDiagnosisKey.toEntityDiagnosisKeys(token, diagnosisKeys)
        );
    }

    @Override
    public void updateDiagnosisKeys(String token, List<DiagnosisKey> diagnosisKeys) {
        EntityToken entityToken = db.daoToken().getToken(token);
        if (entityToken == null) {
            db.daoToken().insertToken(new EntityToken(token, false));
        } else {
            entityToken.exposureDataUpToDate = false;
            db.daoToken().updateToken(entityToken);
        }
        db.daoDiagnosisKey().updateDiagnosisKeys(
            EntityDiagnosisKey.toEntityDiagnosisKeys(token, diagnosisKeys)
        );
    }

    @Override
    public List<DiagnosisKey> getDiagnosisKeys(String token) {
        List<DiagnosisKey> result = new LinkedList<DiagnosisKey>();

        List<EntityDiagnosisKey> entities = db.daoDiagnosisKey().getDiagnosisKeys(token);
        for (EntityDiagnosisKey entity : entities) {
            DiagnosisKey diagnosisKey = new DiagnosisKey(
                new InternalTemporaryExposureKey(
                    new ENInterval(entity.intervalNumber),
                    entity.keyData),
                entity.transmissionRiskLevel
            );
            result.add(diagnosisKey);
        }
        return result;
    }

    @Override
    public void putExposureMatchingResults(
        String token,
        List<ExposureInformation> exposureInformations,
        ExposureSummary exposureSummary
    ) {
        db.daoToken().insertToken(new EntityToken(token, true));
        db.daoExposureInformation().clearDataForToken(token);
        db.daoExposureInformation().insertExposureInformations(
            EntityExposureInformation.toEntityExposureInformations(token, exposureInformations)
        );
        db.daoExposureSummary().clearDataForToken(token);
        db.daoExposureSummary().insertExposureSummary(new EntityExposureSummary(token, exposureSummary));
    }
    // TODO Implement test

    @Override
    public ExposureSummary getExposureSummary(String token) throws StorageException {
        EntityToken entityToken = db.daoToken().getToken(token);
        if (entityToken == null || !entityToken.exposureDataUpToDate) {
            throw new StorageException("db - getExposureSummary(...): No up-to-date exposure data in db");
        }
        return db.daoExposureSummary().getExposureSummary(token).toExposureSummary();
    }
    // TODO Implement test

    @Override
    public List<ExposureInformation> getExposureInformation(String token) throws StorageException {
        EntityToken entityToken = db.daoToken().getToken(token);
        if (entityToken == null || !entityToken.exposureDataUpToDate) {
            throw new StorageException("db - getExposureInformation(...): No up-to-date exposure data in db");
        }
        List<ExposureInformation> result = new LinkedList<>();
        for (EntityExposureInformation e : db.daoExposureInformation().getExposureInformations(token)) {
            result.add(e.toExposureInformation());
        }
        return result;
    }
    // TODO Implement test


    @Override
    public boolean hasTEKForInterval(ENInterval interval) {
        List<EntityTemporaryExposureKey> teks = db.daoTEK().getTekByEnNumber(interval);
        return (teks.size() != 0);
    }


    @Override
    public InternalTemporaryExposureKey getOwnTEK(ENInterval interval) {
        List<EntityTemporaryExposureKey> teks = db.daoTEK().getTekByEnNumber(interval);
        if (teks.size() != 1) {
            throw new StorageException("When attempting to query TEK for interval number " +
                    interval.toString() +
                    ", exactly 1 TEK should be returned from the database, but I found " +
                    teks.size() +
                    " in the database.");
        }
        return teks.get(0).toTemporaryExposureKey();
    }


    @Override
    public Iterable<InternalTemporaryExposureKey> getAllOwnTEKs() {
        List<InternalTemporaryExposureKey> result = new LinkedList<>();
        for (EntityTemporaryExposureKey e : db.daoTEK().getAllGeneratedTEKs()) {
            result.add(e.toTemporaryExposureKey());
        }
        return result;
    }

    @Override
    public Iterable<IntervalOfCapturedData> getAllCollectedPayload() {
        List<EntityCapturedData> allData = db.daoCapturedData().getAllData();

        Map<ENInterval, IntervalOfCapturedData> collectedPackagesByInterval = new HashMap<>();

        for (EntityCapturedData e_payload : allData) {
            CapturedData payload = e_payload.toCapturedData();
            ENInterval interval = payload.getEnInterval();

            // find correct interval
            IntervalOfCapturedData payloadPerInterval
                    = collectedPackagesByInterval.get(interval);
            if (payloadPerInterval == null) {
                payloadPerInterval = new IntervalOfCapturedData(interval);
                collectedPackagesByInterval.put(interval, payloadPerInterval);
            }
            payloadPerInterval.add(payload);
        }

        return collectedPackagesByInterval.values();
    }


    @Override
    public void truncateLast14Days() {
        ENInterval now = ENIntervalUtil.getCurrentInterval();
        long lastIntervalToKeep =
            now.get() - (EnFrameworkConstants.TEK_MAX_STORE_TIME_INTERVALS);

        db.daoCapturedData().truncateOldData(lastIntervalToKeep);
        db.daoTEK().truncateOldData(lastIntervalToKeep);

        // TODO truncate all data, not just teks and captured data
    }


    @Override
    public void deleteTokenWithData(String token) {
        db.daoToken().removeToken(token);
    }

    @Override
    public void clearAllData() {
        db.daoCapturedData().clearAllData();
        db.daoTEK().clearAllData();

        // The following call also clears the diagnosis key, exposure infomation and exposure
        // summary tables, since the items stored there contain token strings as foreign keys:
        db.daoToken().clearAllData();

        // TODO test clear/delete
    }
}
