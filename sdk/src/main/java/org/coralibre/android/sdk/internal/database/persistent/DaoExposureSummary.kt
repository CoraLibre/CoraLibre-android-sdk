package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureSummary

@Dao
interface DaoExposureSummary {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertExposureSummary(entityExposureSummary: EntityExposureSummary)

    @Query("SELECT * FROM EntityExposureSummary WHERE tokenString = :token")
    fun getExposureSummary(token: String): EntityExposureSummary?

    @Query("DELETE FROM EntityExposureSummary WHERE tokenString = :token")
    fun clearDataForToken(token: String)
}
