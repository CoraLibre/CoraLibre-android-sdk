package org.coralibre.android.sdk.internal.database.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.crypto.ENNumber;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;


@Entity
public class EntityGeneratedTEK {

    @PrimaryKey
    public ENNumber interval;

    public byte[] key;


    public EntityGeneratedTEK(){}


    public EntityGeneratedTEK(GeneratedTEK tek) {
        interval = tek.getInterval();
        key = tek.getKey();
    }

    public GeneratedTEK toGeneratedTEK() {
        return new GeneratedTEK(
                interval,
                key
        );
    }


}