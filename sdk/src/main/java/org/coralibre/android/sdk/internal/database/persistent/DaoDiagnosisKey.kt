package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey

@Dao
interface DaoDiagnosisKey {
    // TODO what happens on update?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertDiagnosisKeys(entityDiagnosisKeys: List<EntityDiagnosisKey>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDiagnosisKeys(entityDiagnosisKeys: List<EntityDiagnosisKey>)

    @Query("SELECT * FROM EntityDiagnosisKey WHERE tokenString = :token")
    fun getDiagnosisKeys(token: String): List<EntityDiagnosisKey>
}
