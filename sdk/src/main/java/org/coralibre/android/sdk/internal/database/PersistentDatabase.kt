package org.coralibre.android.sdk.internal.database

import android.content.Context
import androidx.room.Room
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary
import org.coralibre.android.sdk.internal.EnFrameworkConstants
import org.coralibre.android.sdk.internal.database.persistent.RoomDatabaseDelegate
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityCapturedData
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityExposureSummary
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityTemporaryExposureKey
import org.coralibre.android.sdk.internal.database.persistent.entity.EntityToken
import org.coralibre.android.sdk.internal.database.persistent.entity.toEntityDiagnosisKeys
import org.coralibre.android.sdk.internal.database.persistent.entity.toEntityExposureInformations
import org.coralibre.android.sdk.internal.datatypes.CapturedData
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.currentInterval
import java.util.HashMap
import java.util.LinkedList

/**
 * @param context   The context for the database. This is usually the Application context.
 * @param inMemory  If true, an inMemoryDatabaseBuilder is used for creating, resulting in stored data disappearing when the process is killed. Otherwise, a databaseBuilder is used, for aan actually persistent database.
 */
class PersistentDatabase @JvmOverloads constructor(
    context: Context,
    inMemory: Boolean = false
) : Database {

    // TODO: Since we're storing very sensitive data here, we should probably encrypt the database
    //  using SQLCipher
    private val dbName = "coralibre-en-database"
    private val db: RoomDatabaseDelegate = if (inMemory) {
        Room.inMemoryDatabaseBuilder(
            context,
            RoomDatabaseDelegate::class.java
        ).build()
    } else {
        Room.databaseBuilder(
            context,
            RoomDatabaseDelegate::class.java,
            dbName
        ).build()
    }

    override fun addGeneratedTEK(generatedTEK: InternalTemporaryExposureKey) {
        db.daoTEK().insertTEK(EntityTemporaryExposureKey(generatedTEK))
    }

    override fun addCapturedPayload(collectedPayload: CapturedData) {
        db.daoCapturedData().insertCapturedData(EntityCapturedData(collectedPayload))
    }

    override fun addDiagnosisKeys(token: String, diagnosisKeys: List<DiagnosisKey>) {
        var entityToken = db.daoToken().getToken(token)
        if (entityToken == null) {
            db.daoToken().insertToken(EntityToken(token, false))
        } else {
            entityToken = entityToken.copy(exposureDataUpToDate = false)
            db.daoToken().updateToken(entityToken)
        }
        db.daoDiagnosisKey().insertDiagnosisKeys(
            diagnosisKeys.toEntityDiagnosisKeys(token)
        )
    }

    override fun updateDiagnosisKeys(token: String, diagnosisKeys: List<DiagnosisKey>) {
        var entityToken = db.daoToken().getToken(token)
        if (entityToken == null) {
            db.daoToken().insertToken(EntityToken(token, false))
        } else {
            entityToken = entityToken.copy(exposureDataUpToDate = false)
            db.daoToken().updateToken(entityToken)
        }
        db.daoDiagnosisKey().updateDiagnosisKeys(
            diagnosisKeys.toEntityDiagnosisKeys(token)
        )
    }

    override fun getDiagnosisKeys(token: String): List<DiagnosisKey> {
        val result: MutableList<DiagnosisKey> = LinkedList()
        val entities = db.daoDiagnosisKey().getDiagnosisKeys(token)
        for (entity in entities) {
            val diagnosisKey = DiagnosisKey(
                InternalTemporaryExposureKey(
                    ENInterval(entity.intervalNumber),
                    entity.keyData
                ),
                entity.transmissionRiskLevel
            )
            result.add(diagnosisKey)
        }
        return result
    }

    override fun putExposureMatchingResults(
        token: String,
        exposureInformations: List<ExposureInformation>,
        exposureSummary: ExposureSummary
    ) {
        db.daoToken().insertToken(EntityToken(token, true))
        db.daoExposureInformation().clearDataForToken(token)
        db.daoExposureInformation().insertExposureInformations(
            exposureInformations.toEntityExposureInformations(token)
        )
        db.daoExposureSummary().clearDataForToken(token)
        db.daoExposureSummary()
            .insertExposureSummary(EntityExposureSummary(token, exposureSummary))
    }

    // TODO Implement test
    @Throws(StorageException::class)
    override fun getExposureSummary(token: String): ExposureSummary {
        val entityToken = db.daoToken().getToken(token)
        if (entityToken == null || !entityToken.exposureDataUpToDate) {
            throw StorageException("db - getExposureSummary(...): No up-to-date exposure data in db")
        }
        return db.daoExposureSummary().getExposureSummary(token)!!.toExposureSummary()
    }

    // TODO Implement test
    @Throws(StorageException::class)
    override fun getExposureInformation(token: String): List<ExposureInformation> {
        val entityToken = db.daoToken().getToken(token)
        if (entityToken == null || !entityToken.exposureDataUpToDate) {
            throw StorageException("db - getExposureInformation(...): No up-to-date exposure data in db")
        }
        val result: MutableList<ExposureInformation> = LinkedList()
        for (e in db.daoExposureInformation().getExposureInformations(token)) {
            result.add(e.toExposureInformation())
        }
        return result
    }

    // TODO Implement test
    override fun hasTEKForInterval(interval: ENInterval): Boolean {
        val teks = db.daoTEK().getTekByEnNumber(interval)
        return teks.size != 0
    }

    override fun getOwnTEK(interval: ENInterval): InternalTemporaryExposureKey {
        val teks = db.daoTEK().getTekByEnNumber(interval)
        if (teks.size != 1) {
            throw StorageException(
                "When attempting to query TEK for interval number " +
                    interval.toString() +
                    ", exactly 1 TEK should be returned from the database, but I found " +
                    teks.size +
                    " in the database."
            )
        }
        return teks[0].toTemporaryExposureKey()
    }

    override val allOwnTEKs: Iterable<InternalTemporaryExposureKey>
        get() {
            val result: MutableList<InternalTemporaryExposureKey> = LinkedList()
            for (e in db.daoTEK().allGeneratedTEKs) {
                result.add(e.toTemporaryExposureKey())
            }
            return result
        }

    // find correct interval
    override val allCollectedPayload: Iterable<IntervalOfCapturedData>
        get() {
            val allData = db.daoCapturedData().allData
            val collectedPackagesByInterval: MutableMap<ENInterval, IntervalOfCapturedData> =
                HashMap()
            for (e_payload in allData) {
                val payload = e_payload.toCapturedData()
                val interval = payload.enInterval

                // find correct interval
                var payloadPerInterval = collectedPackagesByInterval[interval]
                if (payloadPerInterval == null) {
                    payloadPerInterval = IntervalOfCapturedData(interval)
                    collectedPackagesByInterval[interval] = payloadPerInterval
                }
                payloadPerInterval.add(payload)
            }
            return collectedPackagesByInterval.values
        }

    override fun truncateLast14Days() {
        val now = currentInterval
        val lastIntervalToKeep = now.get() - EnFrameworkConstants.TEK_MAX_STORE_TIME_INTERVALS
        db.daoCapturedData().truncateOldData(lastIntervalToKeep)
        db.daoTEK().truncateOldData(lastIntervalToKeep)

        // TODO truncate all data, not just teks and captured data
    }

    override fun deleteTokenWithData(token: String) {
        db.daoToken().removeToken(token)
    }

    override fun clearAllData() {
        db.daoCapturedData().clearAllData()
        db.daoTEK().clearAllData()

        // The following call also clears the diagnosis key, exposure infomation and exposure
        // summary tables, since the items stored there contain token strings as foreign keys:
        db.daoToken().clearAllData()

        // TODO test clear/delete
    }
}
