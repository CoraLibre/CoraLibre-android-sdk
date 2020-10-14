package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken;


@androidx.room.Database(
        entities = {
            EntityTemporaryExposureKey.class,
            EntityCapturedData.class,
            EntityDiagnosisKey.class,
            EntityToken.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(
        {ENIntervalConverter.class}
)
public abstract class RoomDatabaseDelegate extends RoomDatabase {

    public abstract DaoTEK daoTEK();
    public abstract DaoCapturedData daoCapturedData();
    public abstract DaoDiagnosisKey daoDiagnosisKey();
    public abstract DaoToken daoToken();
}
