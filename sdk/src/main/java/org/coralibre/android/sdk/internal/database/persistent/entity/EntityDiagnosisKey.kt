package org.coralibre.android.sdk.internal.database.persistent.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey

/**
 * @param dbPrimaryKey This field is not used. It is only here to give the database a primary key.
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
data class EntityDiagnosisKey @JvmOverloads constructor(
    val tokenString: String,
    val keyData: ByteArray,
    val intervalNumber: Long,
    val transmissionRiskLevel: Int,
    @PrimaryKey(autoGenerate = true)
    val dbPrimaryKey: Long = 0,
) {
    constructor(token: String, diagnosisKey: DiagnosisKey) : this(
        tokenString = token,
        keyData = diagnosisKey.keyData,
        intervalNumber = diagnosisKey.interval.get(),
        transmissionRiskLevel = diagnosisKey.transmissionRiskLevel,
    )

    companion object {
        @JvmStatic
        fun toEntityDiagnosisKeys(
            token: String,
            diagKeys: List<DiagnosisKey>
        ): List<EntityDiagnosisKey> {
            return diagKeys.toEntityDiagnosisKeys(token)
        }
    }
}

fun List<DiagnosisKey>.toEntityDiagnosisKeys(token: String) = map { EntityDiagnosisKey(token, it) }
