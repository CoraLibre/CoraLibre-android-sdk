package org.coralibre.android.sdk.internal.database.persistent.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey;


@Entity
public class EntityTemporaryExposureKey {

    @PrimaryKey
    public ENInterval interval;

    public byte[] key;


    public EntityTemporaryExposureKey(){}

    public EntityTemporaryExposureKey(InternalTemporaryExposureKey tek) {
        interval = tek.getInterval();
        key = tek.getKey();
    }

    public InternalTemporaryExposureKey toTemporaryExposureKey() {
        return new InternalTemporaryExposureKey(
                interval,
                key
        );
    }


}
