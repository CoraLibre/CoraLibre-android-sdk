package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData

@Dao
interface DaoCapturedData {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertCapturedData(data: EntityCapturedData)

    @Query("DELETE FROM EntityCapturedData WHERE enInterval < :minKeepENIntervalNumber")
    fun truncateOldData(minKeepENIntervalNumber: Long)

    @Query("SELECT * FROM EntityCapturedData WHERE enInterval = :intervalNumber")
    fun getAllDataForSingleInterval(intervalNumber: Long): List<EntityCapturedData>

    @get:Query("SELECT * FROM EntityCapturedData")
    val allData: List<EntityCapturedData>

    // TODO Provide method to remove old data (and also call it somewhere)
    @Query("DELETE FROM EntityCapturedData")
    fun clearAllData()
}
