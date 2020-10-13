package org.coralibre.android.sdk.internal.database.persistent;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.database.model.entity.EntityGeneratedTEK;

import java.util.List;

@Dao
public interface DaoTEK {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertTEK(EntityGeneratedTEK tek);


    @Query("SELECT * FROM EntityGeneratedTEK")
    public List<EntityGeneratedTEK> getAllGeneratedTEKs();


    @Query("DELETE FROM EntityGeneratedTEK WHERE interval < :minKeepENIntervalNumber")
    public void truncateOldData(long minKeepENIntervalNumber);


    @Query("SELECT * FROM EntityGeneratedTEK WHERE interval = :interval")
    public List<EntityGeneratedTEK> getTekByEnNumber(ENInterval interval);


    @Query("DELETE FROM EntityGeneratedTEK")
    public void clearAllData();

}
