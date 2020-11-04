package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureInformation
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureSummary
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken

@Database(
    entities = [
        EntityCapturedData::class,
        EntityDiagnosisKey::class,
        EntityExposureInformation::class,
        EntityExposureSummary::class,
        EntityTemporaryExposureKey::class,
        EntityToken::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    ENIntervalConverter::class
)
abstract class RoomDatabaseDelegate : RoomDatabase() {
    abstract fun daoTEK(): DaoTEK
    abstract fun daoCapturedData(): DaoCapturedData
    abstract fun daoToken(): DaoToken
    abstract fun daoDiagnosisKey(): DaoDiagnosisKey
    abstract fun daoExposureInformation(): DaoExposureInformation
    abstract fun daoExposureSummary(): DaoExposureSummary
}
