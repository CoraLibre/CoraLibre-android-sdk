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
    public long rollingStartNumber;
    public long rollingPeriod;
    public int transmissionRiskLevel;


    public EntityDiagnosisKey() {}

    public EntityDiagnosisKey(final DiagnosisKey diagnosisKey) {
        keyData = diagnosisKey.getKeyData();
        rollingStartNumber = diagnosisKey.getRollingStartNumber().get();
        rollingPeriod = diagnosisKey.getRollingPeriod().get();
        transmissionRiskLevel = diagnosisKey.getTransmissionRiskLevel();
    }

    public DiagnosisKey toDiagnosisKey() {
        return new DiagnosisKey(keyData, rollingStartNumber, rollingPeriod, transmissionRiskLevel);
    }
}
