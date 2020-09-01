package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.coralibre.android.sdk.internal.database.model.entity.EntityCapturedData;
import org.coralibre.android.sdk.internal.database.model.entity.EntityGeneratedTEK;


@androidx.room.Database(
        entities = {EntityGeneratedTEK.class, EntityCapturedData.class},
        version = 1
)
@TypeConverters(
        {ENNumberConverter.class}
        )
public abstract class RoomDatabaseDelegate extends RoomDatabase {

    public abstract DaoTEK daoTEK();
    public abstract DaoCapturedData daoCapturedData();

}
