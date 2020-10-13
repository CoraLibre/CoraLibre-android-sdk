package org.coralibre.android.sdk.internal.database.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(
    entity = EntityToken.class,
    parentColumns = "tokenString",
    childColumns = "tokenString",
    onDelete = CASCADE
))
public class EntityDiagnosisKey {

    /**
     * This field is not used. It is only here to give the database a primary key.
     */
    @PrimaryKey(autoGenerate = true)
    public long dbPrimaryKey;

    public String tokenString;

    public byte[] keyData;
    public long intervalNumber;
    public int transmissionRiskLevel;


    public EntityDiagnosisKey() {}

    public EntityDiagnosisKey(String token, final DiagnosisKey diagnosisKey) {
        tokenString = token;
        keyData = diagnosisKey.getKeyData();
        intervalNumber = diagnosisKey.getInterval().get();
        transmissionRiskLevel = diagnosisKey.getTransmissionRiskLevel();
    }
    
}
