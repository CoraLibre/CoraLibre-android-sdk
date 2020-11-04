package org.coralibre.android.sdk.internal.database.persistent.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary

/**
 * @param tokenString  This field is not used. It is only here to give the database a primary key.
 * When accessing data from the database, we usually filter by timestamp.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EntityToken::class,
            parentColumns = ["tokenString"],
            childColumns = ["tokenString"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EntityExposureSummary(
    @PrimaryKey(autoGenerate = false)
    val tokenString: String,
    val daysSinceLastExposure: Int,
    val matchedKeyCount: Int,
    val maximumRiskScore: Int,
    val summedAttenuationDurationBelow: Int,
    val summedAttenuationDurationBetween: Int,
    val summedAttenuationDurationAbove: Int,
    val summationRiskScore: Int,
) {
    constructor(token: String, summary: ExposureSummary) : this(
        daysSinceLastExposure = summary.daysSinceLastExposure,
        matchedKeyCount = summary.matchedKeyCount,
        maximumRiskScore = summary.maximumRiskScore,
        summedAttenuationDurationBelow = summary.attenuationDurations[0],
        summedAttenuationDurationBetween = summary.attenuationDurations[1],
        summedAttenuationDurationAbove = summary.attenuationDurations[2],
        summationRiskScore = summary.summationRiskScore,
        tokenString = token,
    )

    fun toExposureSummary(): ExposureSummary {
        val attenuationDurations = intArrayOf(
            summedAttenuationDurationBelow,
            summedAttenuationDurationBetween,
            summedAttenuationDurationAbove
        )
        return ExposureSummary(
            daysSinceLastExposure,
            matchedKeyCount,
            maximumRiskScore,
            attenuationDurations,
            summationRiskScore
        )
    }
}
