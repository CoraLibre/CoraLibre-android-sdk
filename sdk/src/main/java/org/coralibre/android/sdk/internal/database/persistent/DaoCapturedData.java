package org.coralibre.android.sdk.internal.database.persistent;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData;

import java.util.List;

@Dao
public interface DaoCapturedData {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertCapturedData(EntityCapturedData data);


    @Query("DELETE FROM EntityCapturedData WHERE enInterval < :minKeepENIntervalNumber")
    public void truncateOldData(long minKeepENIntervalNumber);


    @Query("SELECT * FROM EntityCapturedData WHERE enInterval = :intervalNumber")
    public List<EntityCapturedData> getAllDataForSingleInterval(long intervalNumber);


    @Query("SELECT * FROM EntityCapturedData")
    public List<EntityCapturedData> getAllData();


    // TODO Provide method to remove old data (and also call it somewhere)


    @Query("DELETE FROM EntityCapturedData")
    public void clearAllData();

}
