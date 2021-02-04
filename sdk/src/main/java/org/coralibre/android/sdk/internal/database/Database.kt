package org.coralibre.android.sdk.internal.database

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary
import org.coralibre.android.sdk.internal.datatypes.CapturedData
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData
import kotlin.Throws

interface Database {
    // TODO Throw exceptions when problems occur (e.g. no data for queried token)
    //  BUT take care that empty data is handled correctly everywhere (since empty != non existent)

    fun addGeneratedTEK(generatedTEK: InternalTemporaryExposureKey)
    fun getAllOwnTEKs(): Iterable<InternalTemporaryExposureKey>

    /**
     * @param interval An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    @Throws(StorageException::class)
    fun getOwnTEK(interval: ENInterval): InternalTemporaryExposureKey
    fun hasTEKForInterval(interval: ENInterval): Boolean
    fun addCapturedPayload(collectedPayload: CapturedData)
    fun getAllCollectedPayload(): Iterable<IntervalOfCapturedData>

    /**
     * @param token A token to identify the key set later on. If the token has already been used
     * previously to add diagnosis keys, it now identifies the previously existing
     * keys together with the new ones contained in 'diagnosisKeys'.
     */
    fun addDiagnosisKeys(token: String, diagnosisKeys: List<DiagnosisKey>)
    fun updateDiagnosisKeys(token: String, diagnosisKeys: List<DiagnosisKey>)

    /**
     * @param token A token with that an addDiagnosisKeys(...) call has been performed previously
     * @throws StorageException if the token is unknown to the db
     */
    @Throws(StorageException::class)
    fun getDiagnosisKeys(token: String): List<DiagnosisKey>

    /**
     * Adds a set of ExposureInformation items as well as a a single ExposureSummary
     * item to the database. If there already is data for the given token, the data
     * will be overwritten.
     * @param token A token to identify the data later on.
     * @param exposureInformations the information ExposureInformation objects to store
     * @param exposureSummary the ExposureSummary object to store
     */
    fun putExposureMatchingResults(
        token: String,
        exposureInformations: List<ExposureInformation>,
        exposureSummary: ExposureSummary
    )

    /**
     * Get an exposure summary object that has previously been stored using
     * 'putExposureMatchingResults(...)'. If there is no exposure summary, or it is not up-to-date
     * (i.e. further diagnosis keys have been added for the token without adding new exposure
     * matching results afterwards), an exception is thrown.
     * @param token to identify the ExposureSummary object
     * @throws StorageException if there is no ExposureSummary for that token, which is up-to-date
     */
    @Throws(StorageException::class)
    fun getExposureSummary(token: String): ExposureSummary

    /**
     * Get exposure information objects that have previously been stored using
     * 'putExposureMatchingResults(...)'. If there is no such data, or it is not up-to-date
     * (i.e. further diagnosis keys have been added for the token without adding new exposure
     * matching results afterwards), an exception is thrown.
     * @param token to identify the set of ExposureInformation objects
     * @throws StorageException if there is no matchin result data for that token, which is up-to-date
     */
    @Throws(StorageException::class)
    fun getExposureInformation(token: String): List<ExposureInformation>

    /**
     * @param token A token with that an addDiagnosisKeys(...) call has been performed previously
     * @throws StorageException if the token is unknown to the db
     */
    @Throws(StorageException::class)
    fun deleteTokenWithData(token: String)

    // TODO also delete computed exposure information and summary
    // TODO when is this method actually called?
    fun truncateLast14Days()
    fun clearAllData()

    fun close() {}
}
