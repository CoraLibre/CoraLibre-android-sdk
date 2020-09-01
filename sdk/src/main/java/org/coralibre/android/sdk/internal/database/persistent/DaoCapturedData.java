package org.coralibre.android.sdk.internal.database.persistent;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.database.model.entity.EntityCapturedData;

import java.util.List;

@Dao
public interface DaoCapturedData {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertCapturedData(EntityCapturedData data);


    @Query("DELETE FROM EntityCapturedData WHERE enNumber < :minKeepENIntervalNumber")
    public void truncateOldData(long minKeepENIntervalNumber);


    @Query("SELECT * FROM EntityCapturedData WHERE enNumber = :intervalNumber")
    public List<EntityCapturedData> getAllDataForSingleInterval(long intervalNumber);


    @Query("SELECT * FROM EntityCapturedData")
    public List<EntityCapturedData> getAllData();


    @Query("DELETE FROM EntityCapturedData")
    public void clearAllData();

}
