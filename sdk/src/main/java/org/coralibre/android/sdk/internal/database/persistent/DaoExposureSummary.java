package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureSummary;


@Dao
public interface DaoExposureSummary {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertExposureSummary (EntityExposureSummary entityExposureSummary);


    @Query("SELECT * FROM EntityExposureSummary WHERE tokenString = :token")
    public EntityExposureSummary getExposureSummary(String token);


    @Query("DELETE FROM EntityExposureSummary WHERE tokenString = :token")
    public void clearDataForToken(String token);

}
