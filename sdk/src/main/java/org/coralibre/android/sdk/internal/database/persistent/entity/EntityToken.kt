package org.coralibre.android.sdk.internal.database.persistent.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// TODO add some timestamp, so that data for tokens that have not been used for some time can
//  deleted by some background service. Perform such auto deletion somewhere.

/**
 * @param exposureDataUpToDate True iff we already have ExposureInformation and
 * ExposureSummary data for the newest diagnosis key set for this token.
 * @param timestampMillisSinceEpoch Timestamp storing the moment when this token has been
 * added to the database.
 */
@Entity
data class EntityToken @JvmOverloads constructor(
    @PrimaryKey val tokenString: String,
    val exposureDataUpToDate: Boolean,
    val timestampMillisSinceEpoch: Long = Date().time,
)
