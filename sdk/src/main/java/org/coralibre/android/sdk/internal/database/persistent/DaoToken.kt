package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken

@Dao
interface DaoToken {
    // TODO test inserting, adding after first insertion and deleting.
    // TODO mplement removal of tokens after they are not required anymore. Regard the special case,
    //  that an app might always use the same single token string

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertToken(token: EntityToken)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun updateToken(token: EntityToken)

    @Query("SELECT * FROM EntityToken WHERE tokenString = :tokenString")
    fun getToken(tokenString: String): EntityToken?

    @Query("DELETE FROM EntityToken WHERE tokenString = :tokenString")
    fun removeToken(tokenString: String)

    @Query("DELETE FROM EntityToken")
    fun clearAllData()
}
