package org.coralibre.android.sdk.internal.database.persistent.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class EntityToken {

    // TODO add some timestamp, so that data for tokens that have not been used for some time can
    //  deleted by some background service. Perform such auto deletion somewhere.

    @PrimaryKey @NonNull
    public String tokenString;

    /**
     * Timestamp storing the moment when this token has been added to the database.
     */
    public long timestampMillisSinceEpoch;

    /**
     * True iff we already have ExposureInformation and ExposureSummary data for the
     * newest diagnosis key set for this token.
     */
    public boolean exposureDataUpToDate;


    public EntityToken(final String tokenString, boolean exposureDataUpToDate) {
        this.tokenString = tokenString;
        this.timestampMillisSinceEpoch = new Date().getTime();
        this.exposureDataUpToDate = exposureDataUpToDate;
    }

}
