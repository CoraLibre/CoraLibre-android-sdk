package org.coralibre.android.sdk.internal.database.persistent;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;

import java.util.List;

@Dao
public interface DaoTEK {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertTEK(EntityTemporaryExposureKey tek);


    @Query("SELECT * FROM EntityTemporaryExposureKey")
    public List<EntityTemporaryExposureKey> getAllGeneratedTEKs();


    @Query("DELETE FROM EntityTemporaryExposureKey WHERE interval < :minKeepENIntervalNumber")
    public void truncateOldData(long minKeepENIntervalNumber);


    @Query("SELECT * FROM EntityTemporaryExposureKey WHERE interval = :interval")
    public List<EntityTemporaryExposureKey> getTekByEnNumber(ENInterval interval);


    @Query("DELETE FROM EntityTemporaryExposureKey")
    public void clearAllData();

}
