package org.coralibre.android.sdk.internal.database.model.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.database.model.CapturedData;


@Entity
public class EntityCapturedData {

    /**
     * This field is not used. It is only here to give the database a primary key. When accessing
     * data from the database, we usually filter by timestamp.
     */
    @PrimaryKey(autoGenerate = true)
    public long dbPrimaryKey;

    /**
     * in milliseconds since Epoch
     */
    public long captureTimestamp;
    public ENInterval enInterval;
    public byte rssi;
    public byte[] rpi;
    public byte[] aem;


    public EntityCapturedData(){}


    public EntityCapturedData(CapturedData data) {
        captureTimestamp = data.getCaptureTimestamp();
        enInterval = data.getEnInterval();
        rssi = data.getRssi();
        rpi = data.getRpi();
        aem = data.getAem();
    }

    public CapturedData toCapturedData() {
        return new CapturedData(
                captureTimestamp,
                rssi,
                rpi,
                aem
        );
    }

}
