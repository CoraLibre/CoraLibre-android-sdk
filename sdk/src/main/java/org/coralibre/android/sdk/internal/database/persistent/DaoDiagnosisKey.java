package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.coralibre.android.sdk.internal.database.model.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.entity.EntityGeneratedTEK;

import java.util.List;

@Dao
public interface DaoDiagnosisKey {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // replace in case of conflict
    public void insertTEK(EntityGeneratedTEK tek);

    @Query("SELECT * FROM EntityDiagnosisKey")
    public List<EntityDiagnosisKey> getAllDiagnosisKeys();
}
