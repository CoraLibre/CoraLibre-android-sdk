package org.coralibre.android.sdk.internal.database.persistent;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken;

@Dao
public interface DaoToken {

    // TODO test inserting, adding after first insertion and deleting.
    // TODO mplement removal of tokens after they are not required anymore. Regard the special case,
    //  that an app might always use the same single token string


    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertToken(EntityToken token);

    @Update(onConflict = OnConflictStrategy.ABORT)
    public void updateToken(EntityToken token);

    @Query("SELECT * FROM EntityToken WHERE tokenString = :tokenString")
    public EntityToken getToken(String tokenString);

    @Query("DELETE FROM EntityToken WHERE tokenString = :tokenString")
    public void removeToken(String tokenString);


    @Query("DELETE FROM EntityToken")
    public void clearAllData();

}
