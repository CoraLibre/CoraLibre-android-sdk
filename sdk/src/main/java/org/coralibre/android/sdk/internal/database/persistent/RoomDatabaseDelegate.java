package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.coralibre.android.sdk.internal.database.model.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.model.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.entity.EntityTemporaryExposureKey;


@androidx.room.Database(
        entities = {EntityTemporaryExposureKey.class, EntityCapturedData.class, EntityDiagnosisKey.class},
        version = 1
)
@TypeConverters(
        {ENIntervalConverter.class}
)
public abstract class RoomDatabaseDelegate extends RoomDatabase {

    public abstract DaoTEK daoTEK();
    public abstract DaoCapturedData daoCapturedData();
    public abstract DaoDiagnosisKey daoDiagnosisKey();
}
