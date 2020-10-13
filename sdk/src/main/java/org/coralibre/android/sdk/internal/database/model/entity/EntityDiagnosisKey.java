package org.coralibre.android.sdk.internal.database.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;

@Entity
public class EntityDiagnosisKey {

    /**
     * This field is not used. It is only here to give the database a primary key.
     */
    @PrimaryKey(autoGenerate = true)
    public long dbPrimaryKey;

    public byte[] keyData;
    public long intervalNumber;
    public int transmissionRiskLevel;


    public EntityDiagnosisKey() {}

    public EntityDiagnosisKey(final DiagnosisKey diagnosisKey) {
        keyData = diagnosisKey.getKeyData();
        intervalNumber = diagnosisKey.getInterval().get();
        transmissionRiskLevel = diagnosisKey.getTransmissionRiskLevel();
    }

    public DiagnosisKey toDiagnosisKey() {
        return new DiagnosisKey(keyData, intervalNumber, transmissionRiskLevel);
    }
}
