package org.coralibre.android.sdk.internal.database.persistent.entity;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier;


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
        rpi = data.getRpi().getData();
        aem = data.getAem().getData();
    }

    public CapturedData toCapturedData() {
        return new CapturedData(
                captureTimestamp,
                rssi,
                new RollingProximityIdentifier(rpi, enInterval),
                new AssociatedEncryptedMetadata(aem)
        );
    }

}
