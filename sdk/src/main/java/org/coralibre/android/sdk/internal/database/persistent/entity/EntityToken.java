package org.coralibre.android.sdk.internal.database.persistent.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class EntityToken {

    @PrimaryKey @NonNull
    public String tokenString;


    public EntityToken(final String tokenString) {
        this.tokenString = tokenString;
    }

    public EntityToken toEntityToken(String tokenString) {
        return new EntityToken(tokenString);
    }
}
