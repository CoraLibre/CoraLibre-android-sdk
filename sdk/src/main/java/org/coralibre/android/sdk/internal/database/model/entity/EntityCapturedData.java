package org.coralibre.android.sdk.internal.database.model.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.database.model.CapturedData;


@Entity
public class EntityCapturedData {

    @PrimaryKey(autoGenerate = true)
    public long dbPrimaryKey;
    // This field is not used. It is only here to give the database a primary key. When
    // accessing data from the database, we usually filter by timestamp.

    public long captureTimestamp; // in milliseconds since Epoch
    public ENNumber enNumber;
    public byte rssi;
    public byte[] payload;


    public EntityCapturedData(){}


    public EntityCapturedData(CapturedData data) {
        captureTimestamp = data.getCaptureTimestamp();
        enNumber = data.getEnNumber();
        rssi = data.getRssi();
        payload = data.getPayload();
    }

    public CapturedData toCapturedData() {
        return new CapturedData(
                captureTimestamp,
                rssi,
                payload
        );
    }

}
