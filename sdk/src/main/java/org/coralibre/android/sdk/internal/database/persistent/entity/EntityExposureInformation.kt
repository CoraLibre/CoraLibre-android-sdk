package org.coralibre.android.sdk.internal.database.persistent.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation.ExposureInformationBuilder
import java.util.LinkedList

/**
 * @param dbPrimaryKey This field is not used. It is only here to give the database a primary key.
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
data class EntityExposureInformation @JvmOverloads constructor(
    val tokenString: String,
    val dateMillisSinceEpoch: Long,
    val durationMinutes: Int,
    val attenuationValue: Int,
    val transmissionRiskLevel: Int,
    val totalRiskScore: Int,
    val attenuationDurationBelow: Int,
    val attenuationDurationBetween: Int,
    val attenuationDurationAbove: Int,
    @PrimaryKey(autoGenerate = true)
    val dbPrimaryKey: Long = 0,
) {
    constructor(token: String, info: ExposureInformation) : this(
        tokenString = token,
        dateMillisSinceEpoch = info.dateMillisSinceEpoch,
        durationMinutes = info.durationMinutes,
        attenuationValue = info.attenuationValue,
        transmissionRiskLevel = info.transmissionRiskLevel,
        totalRiskScore = info.totalRiskScore,
        attenuationDurationBelow = info.attenuationDurationsInMinutes[0],
        attenuationDurationBetween = info.attenuationDurationsInMinutes[1],
        attenuationDurationAbove = info.attenuationDurationsInMinutes[2],
    )

    fun toExposureInformation(): ExposureInformation {
        val attenuationDurations = intArrayOf(
            attenuationDurationBelow,
            attenuationDurationBetween,
            attenuationDurationAbove
        )
        return ExposureInformationBuilder()
            .setDateMillisSinceEpoch(dateMillisSinceEpoch)
            .setDurationMinutes(durationMinutes)
            .setAttenuationValue(attenuationValue)
            .setTransmissionRiskLevel(transmissionRiskLevel)
            .setTotalRiskScore(totalRiskScore)
            .setAttenuationDurations(attenuationDurations)
            .build()
    }

    companion object {
        @JvmStatic
        fun toEntityExposureInformations(
            token: String,
            infos: List<ExposureInformation>
        ): List<EntityExposureInformation> {
            val result = LinkedList<EntityExposureInformation>()
            for (info in infos) {
                result.add(EntityExposureInformation(token, info))
            }
            return result
        }
    }
}

fun List<ExposureInformation>.toEntityExposureInformations(
    token: String
): List<EntityExposureInformation> = map { EntityExposureInformation(token, it) }
