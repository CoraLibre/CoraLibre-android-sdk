package org.coralibre.android.sdk.internal.database.persistent.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier;

import static androidx.room.ForeignKey.CASCADE;


@Entity(foreignKeys = @ForeignKey(
    entity = EntityToken.class,
    parentColumns = "tokenString",
    childColumns = "tokenString",
    onDelete = CASCADE
))
public class EntityExposureSummary {

    /**
     * This field is not used. It is only here to give the database a primary key. When accessing
     * data from the database, we usually filter by timestamp.
     */
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String tokenString;

    public int daysSinceLastExposure;
    public int matchedKeyCount;
    public int maximumRiskScore;
    public int summedAttenuationDurationBelow;
    public int summedAttenuationDurationBetween;
    public int summedAttenuationDurationAbove;
    public int summationRiskScore;


    public EntityExposureSummary(){}

    public EntityExposureSummary(String token, ExposureSummary summary) {
        daysSinceLastExposure = summary.getDaysSinceLastExposure();
        matchedKeyCount = summary.getMatchedKeyCount();
        maximumRiskScore = summary.getMaximumRiskScore();
        summedAttenuationDurationBelow = summary.getAttenuationDurations()[0];
        summedAttenuationDurationBetween = summary.getAttenuationDurations()[1];
        summedAttenuationDurationAbove = summary.getAttenuationDurations()[2];
        summationRiskScore = summary.getSummationRiskScore();
        tokenString = token;
    }

    public ExposureSummary toExposureSummary() {
        int[] attenuationDurations = {
            summedAttenuationDurationBelow,
            summedAttenuationDurationBetween,
            summedAttenuationDurationAbove
        };
        return new ExposureSummary(
            daysSinceLastExposure,
            matchedKeyCount,
            maximumRiskScore,
            attenuationDurations,
            summationRiskScore
        );
    }

}
