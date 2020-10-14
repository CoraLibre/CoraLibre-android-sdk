package org.coralibre.android.sdk.internal.database.persistent.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.TemporaryExposureKey_internal;


@Entity
public class EntityTemporaryExposureKey {

    @PrimaryKey
    public ENInterval interval;

    public byte[] key;


    public EntityTemporaryExposureKey(){}

    public EntityTemporaryExposureKey(TemporaryExposureKey_internal tek) {
        interval = tek.getInterval();
        key = tek.getKey();
    }

    public TemporaryExposureKey_internal toTemporaryExposureKey() {
        return new TemporaryExposureKey_internal(
                interval,
                key
        );
    }


}
