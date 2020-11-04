package org.coralibre.android.sdk.internal.database.persistent.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey

@Entity
data class EntityTemporaryExposureKey(
    @PrimaryKey
    val interval: ENInterval,
    val key: ByteArray,
) {
    constructor(tek: InternalTemporaryExposureKey) : this(
        interval = tek.interval,
        key = tek.key,
    )

    fun toTemporaryExposureKey(): InternalTemporaryExposureKey {
        return InternalTemporaryExposureKey(interval, key)
    }
}
