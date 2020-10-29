package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureInformation;

import java.util.List;

@Dao
public interface DaoExposureInformation {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertExposureInformations(List<EntityExposureInformation> entityExposureInformations);

    @Query("SELECT * FROM EntityExposureInformation WHERE tokenString = :token")
    public List<EntityExposureInformation> getExposureInformations(String token);


    @Query("DELETE FROM EntityExposureInformation WHERE tokenString = :token")
    public void clearDataForToken(String token);

}
