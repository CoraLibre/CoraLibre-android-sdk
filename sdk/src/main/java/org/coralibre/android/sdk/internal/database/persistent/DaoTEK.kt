package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey
import org.coralibre.android.sdk.internal.datatypes.ENInterval

@Dao
interface DaoTEK {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertTEK(tek: EntityTemporaryExposureKey)

    @get:Query("SELECT * FROM EntityTemporaryExposureKey")
    val allGeneratedTEKs: List<EntityTemporaryExposureKey>

    @Query("DELETE FROM EntityTemporaryExposureKey WHERE interval < :minKeepENIntervalNumber")
    fun truncateOldData(minKeepENIntervalNumber: Long)

    @Query("SELECT * FROM EntityTemporaryExposureKey WHERE interval = :interval")
    fun getTekByEnNumber(interval: ENInterval?): List<EntityTemporaryExposureKey>

    @Query("DELETE FROM EntityTemporaryExposureKey")
    fun clearAllData()
}
