package org.coralibre.android.sdk.internal.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import org.coralibre.android.sdk.internal.crypto.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.crypto.TemporaryExposureKey_internal;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedDataImpl;
import org.coralibre.android.sdk.internal.database.model.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.model.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.entity.EntityTemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.persistent.RoomDatabaseDelegate;
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PersistentDatabase implements Database {

    // TODO: Since we're storing very sensitive data here, we should probably encrypt the database
    //  using SQLCipher


    private final String dbName = "db";
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
    public void addGeneratedTEK(TemporaryExposureKey_internal generatedTEK) {
        db.daoTEK().insertTEK(new EntityTemporaryExposureKey(generatedTEK));
    }

    @Override
    public void addCapturedPayload(CapturedData collectedPayload) {
        db.daoCapturedData().insertCapturedData(new EntityCapturedData(collectedPayload));
    }


    private static List<EntityDiagnosisKey> toEntityDiagnosisKeys(List<TemporaryExposureKeyProto> tekProtos) {
        List<EntityDiagnosisKey> entityDiagnosisKeys = new ArrayList<>();
        for (TemporaryExposureKeyProto tekProto : tekProtos) {
            if (!tekProto.hasKeyData()) {
                throw new IllegalArgumentException("missing tekProto keyData");
            } else if (!tekProto.hasRollingStartIntervalNumber()) {
                throw new IllegalArgumentException("missing tekProto rollingStartIntervalNumber");
            } else if (!tekProto.hasRollingPeriod()) {
                throw new IllegalArgumentException("missing tekProto rollingPeriod");
            }

            entityDiagnosisKeys.add(new EntityDiagnosisKey(new DiagnosisKey(
                tekProto.getKeyData().toByteArray(),
                tekProto.getRollingStartIntervalNumber(),
                tekProto.hasTransmissionRiskLevel() ? tekProto.getTransmissionRiskLevel() : 0)));
        }
        return entityDiagnosisKeys;
    }

    @Override
    public void addDiagnosisKeys(List<TemporaryExposureKeyProto> diagnosisKeys) {
        db.daoDiagnosisKey().insertDiagnosisKeys(toEntityDiagnosisKeys(diagnosisKeys));
    }

    @Override
    public void updateDiagnosisKeys(List<TemporaryExposureKeyProto> diagnosisKeys) {
        db.daoDiagnosisKey().updateDiagnosisKeys(toEntityDiagnosisKeys(diagnosisKeys));
    }

    @Override
    public List<DiagnosisKey> getAllDiagnosisKeys() {
        List<DiagnosisKey> result = new LinkedList<DiagnosisKey>();

        List<EntityDiagnosisKey> entities = db.daoDiagnosisKey().getAllDiagnosisKeys();
        for (EntityDiagnosisKey entity : entities) {
            DiagnosisKey diagnosisKey = new DiagnosisKey(
                entity.keyData,
                entity.intervalNumber,
                entity.transmissionRiskLevel
            );
            result.add(diagnosisKey);
        }
        return result;
    }


    @Override
    public boolean hasTEKForInterval(ENInterval interval) {
        List<EntityTemporaryExposureKey> teks = db.daoTEK().getTekByEnNumber(interval);
        return (teks.size() != 0);
    }


    @Override
    public TemporaryExposureKey_internal getOwnTEK(ENInterval interval) {
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
    public Iterable<TemporaryExposureKey_internal> getAllOwnTEKs() {
        List<TemporaryExposureKey_internal> result = new LinkedList<>();
        for (EntityTemporaryExposureKey e : db.daoTEK().getAllGeneratedTEKs()) {
            result.add(e.toTemporaryExposureKey());
        }
        return result;
    }

    @Override
    public Iterable<IntervalOfCapturedData> getAllCollectedPayload() throws Exception {
        List<EntityCapturedData> allData = db.daoCapturedData().getAllData();

        Map<ENInterval, IntervalOfCapturedData> collectedPackagesByInterval = new HashMap<>();

        for (EntityCapturedData e_payload : allData) {
            CapturedData payload = e_payload.toCapturedData();
            ENInterval interval = payload.getEnInterval();

            // find correct interval
            IntervalOfCapturedData payloadPerInterval
                    = collectedPackagesByInterval.get(interval);
            if (payloadPerInterval == null) {
                payloadPerInterval = new IntervalOfCapturedDataImpl(interval);
                collectedPackagesByInterval.put(interval, payloadPerInterval);
            }
            payloadPerInterval.add(payload);
        }

        return collectedPackagesByInterval.values();
    }


    @Override
    public void truncateLast14Days() {
        ENInterval now = CryptoModule.getCurrentInterval();
        long lastIntervalToKeep = now.get() -
                (CryptoModule.TEK_MAX_STORE_TIME
                        * TemporaryExposureKey_internal.TEK_ROLLING_PERIOD);

        db.daoCapturedData().truncateOldData(lastIntervalToKeep);
        db.daoTEK().truncateOldData(lastIntervalToKeep);
    }


    @Override
    public void clearAllData() {
        db.daoCapturedData().clearAllData();
        db.daoTEK().clearAllData();
    }
}
