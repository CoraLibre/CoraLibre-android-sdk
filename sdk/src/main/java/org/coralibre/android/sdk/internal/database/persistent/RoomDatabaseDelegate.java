package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureInformation;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureSummary;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey;
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken;


@androidx.room.Database(
        entities = {
            EntityTemporaryExposureKey.class,
            EntityCapturedData.class,
            EntityToken.class,
            EntityDiagnosisKey.class,
            EntityExposureInformation.class,
            EntityExposureSummary.class
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
    public abstract DaoToken daoToken();
    public abstract DaoDiagnosisKey daoDiagnosisKey();
    public abstract DaoExposureInformation daoExposureInformation();
    public abstract DaoExposureSummary daoExposureSummary();

}
