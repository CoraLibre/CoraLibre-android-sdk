package org.coralibre.android.sdk.internal.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedDataImpl;
import org.coralibre.android.sdk.internal.database.model.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.model.entity.EntityGeneratedTEK;
import org.coralibre.android.sdk.internal.database.persistent.RoomDatabaseDelegate;

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
    public void addGeneratedTEK(GeneratedTEK generatedTEK) {
        db.daoTEK().insertTEK(
                new EntityGeneratedTEK(generatedTEK)
        );
    }


    @Override
    public void addCapturedPayload(CapturedData collectedPayload) {
        db.daoCapturedData().insertCapturedData(
                new EntityCapturedData(collectedPayload)
        );
    }


    @Override
    public boolean hasTEKForInterval(ENNumber interval) {
        List<EntityGeneratedTEK> teks = db.daoTEK().getTekByEnNumber(interval);
        return (teks.size() != 0);
    }


    @Override
    public GeneratedTEK getGeneratedTEK(ENNumber interval) {
        List<EntityGeneratedTEK> teks = db.daoTEK().getTekByEnNumber(interval);
        if (teks.size() != 1) {
            throw new StorageException("When attempting to query TEK for interval number " +
                    interval.toString() +
                    ", exactly 1 TEK should be returned from the database, but I found " +
                    teks.size() +
                    " in the database.");
        }
        return teks.get(0).toGeneratedTEK();
    }


    @Override
    public Iterable<GeneratedTEK> getAllGeneratedTEKs() {
        List<GeneratedTEK> result = new LinkedList<>();
        for (EntityGeneratedTEK e : db.daoTEK().getAllGeneratedTEKs()) {
            result.add(e.toGeneratedTEK());
        }
        return result;
    }


    @Override
    public Iterable<IntervalOfCapturedData> getAllCollectedPayload() {
        List<EntityCapturedData> allData = db.daoCapturedData().getAllData();

        Map<ENNumber, IntervalOfCapturedData> collectedPackagesByInterval = new HashMap<>();

        for (EntityCapturedData e_payload : allData) {
            CapturedData payload = e_payload.toCapturedData();
            ENNumber interval = payload.getEnNumber();

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
        ENNumber now = CryptoModule.getCurrentInterval();
        long lastIntervalToKeep = now.get() -
                (CryptoModule.TEK_MAX_STORE_TIME
                        * TemporaryExposureKey.TEK_ROLLING_PERIOD);

        db.daoCapturedData().truncateOldData(lastIntervalToKeep);
        db.daoTEK().truncateOldData(lastIntervalToKeep);
    }


    @Override
    public void clearAllData() {
        db.daoCapturedData().clearAllData();
        db.daoTEK().clearAllData();
    }
}
