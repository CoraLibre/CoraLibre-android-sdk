package org.coralibre.android.sdk.internal.database.persistent.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;

import java.util.LinkedList;
import java.util.List;

import static androidx.room.ForeignKey.CASCADE;


@Entity(foreignKeys = @ForeignKey(
    entity = EntityToken.class,
    parentColumns = "tokenString",
    childColumns = "tokenString",
    onDelete = CASCADE
))
public class EntityExposureInformation {

    /**
     * This field is not used. It is only here to give the database a primary key. When accessing
     * data from the database, we usually filter by timestamp.
     */
    @PrimaryKey(autoGenerate = true)
    public long dbPrimaryKey;

    public String tokenString;

    public long dateMillisSinceEpoch;
    public int durationMinutes;
    public int attenuationValue;
    public int transmissionRiskLevel;
    public int totalRiskScore;
    public int attenuationDurationBelow;
    public int attenuationDurationBetween;
    public int attenuationDurationAbove;


    public EntityExposureInformation(){}


    public EntityExposureInformation(String token, ExposureInformation info) {
        tokenString = token;

        dateMillisSinceEpoch = info.getDateMillisSinceEpoch();
        durationMinutes = info.getDurationMinutes();
        attenuationValue = info.getAttenuationValue();
        transmissionRiskLevel = info.getTransmissionRiskLevel();
        totalRiskScore = info.getTotalRiskScore();
        attenuationDurationBelow = info.getAttenuationDurationsInMinutes()[0];
        attenuationDurationBetween = info.getAttenuationDurationsInMinutes()[1];
        attenuationDurationAbove = info.getAttenuationDurationsInMinutes()[2];
    }

    public ExposureInformation toExposureInformation() {
        int[] attenuationDurations = {
            attenuationDurationBelow,
            attenuationDurationBetween,
            attenuationDurationAbove
        };
        return new ExposureInformation.ExposureInformationBuilder()
            .setDateMillisSinceEpoch(dateMillisSinceEpoch)
            .setDurationMinutes(durationMinutes)
            .setAttenuationValue(attenuationValue)
            .setTransmissionRiskLevel(transmissionRiskLevel)
            .setTotalRiskScore(totalRiskScore)
            .setAttenuationDurations(attenuationDurations)
            .build();
    }



    public static List<EntityExposureInformation> toEntityExposureInformations(
        String token, List<ExposureInformation> infos
    ) {
        LinkedList<EntityExposureInformation> result = new LinkedList<>();
        for (ExposureInformation info : infos) {
            result.add(new EntityExposureInformation(token, info));
        }
        return result;
    }

}
