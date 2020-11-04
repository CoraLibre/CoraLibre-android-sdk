package org.coralibre.android.sdk.internal.database.persistent.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata
import org.coralibre.android.sdk.internal.datatypes.CapturedData
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier

/**
 * @param dbPrimaryKey This field is not used. It is only here to give the database a primary key.
 * When accessing data from the database, we usually filter by timestamp.
 * @param captureTimestamp in milliseconds since Epoch
 */
@Entity
data class EntityCapturedData(
    @PrimaryKey(autoGenerate = true)
    var dbPrimaryKey: Long = 0,
    val captureTimestamp: Long = 0,
    val enInterval: ENInterval,
    val rssi: Byte = 0,
    val rpi: ByteArray = ByteArray(0),
    val aem: ByteArray = ByteArray(0),
) {
    constructor(data: CapturedData) : this(
        captureTimestamp = data.captureTimestampMillis,
        enInterval = data.enInterval,
        rssi = data.rssi,
        rpi = data.rpi.getData(),
        aem = data.aem.data
    )

    fun toCapturedData(): CapturedData {
        return CapturedData(
            captureTimestamp,
            rssi,
            RollingProximityIdentifier(rpi, enInterval),
            AssociatedEncryptedMetadata(aem)
        )
    }
}
