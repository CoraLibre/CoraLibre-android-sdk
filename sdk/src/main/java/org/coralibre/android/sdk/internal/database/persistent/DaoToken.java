package org.coralibre.android.sdk.internal.database.persistent;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.database.model.entity.EntityTemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.model.entity.EntityToken;

import java.util.List;

@Dao
public interface DaoToken {

    //TODO test inserting, adding after first insertion and deleting.
    // Also ensure/test that a token is dropped, once no diagnosis key set references it anymore


    // Conflicts are ignored, since multiple insertions for the same token are possible
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insertToken(EntityToken token);

    @Query("DELETE FROM EntityToken WHERE tokenString = :tokenString")
    public void removeToken(String tokenString);

    @Query("DELETE FROM EntityToken")
    public void clearAllData();


    // TODO Clean tokens that are not in use, i.e. that are not referenced by stored diagnosis keys

}
