package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureInformation

@Dao
interface DaoExposureInformation {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertExposureInformations(entityExposureInformations: List<EntityExposureInformation>)

    @Query("SELECT * FROM EntityExposureInformation WHERE tokenString = :token")
    fun getExposureInformations(token: String): List<EntityExposureInformation>

    @Query("DELETE FROM EntityExposureInformation WHERE tokenString = :token")
    fun clearDataForToken(token: String)
}
