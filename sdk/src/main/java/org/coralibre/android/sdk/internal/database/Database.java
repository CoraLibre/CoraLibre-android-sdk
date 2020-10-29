package org.coralibre.android.sdk.internal.database;


import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey;

import java.util.List;

public interface Database {

    // TODO Throw exceptions when problems occur (e.g. no data for queried token)
    //  BUT take care that empty data is handled correctly everywhere (since empty != non existent)


    void addGeneratedTEK(InternalTemporaryExposureKey generatedTEK);

    Iterable<InternalTemporaryExposureKey> getAllOwnTEKs();

    /**
     * @param interval An interval for that a temporary exposure key exists
     * @throws StorageException if there is no key for that interval
     */
    InternalTemporaryExposureKey getOwnTEK(ENInterval interval) throws StorageException;

    boolean hasTEKForInterval(ENInterval interval);



    void addCapturedPayload(CapturedData collectedPayload);

    Iterable<IntervalOfCapturedData> getAllCollectedPayload();


    /**
     * @param token A token to identify the key set later on. If the token has already been used
     *              previously to add diagnosis keys, it now identifies the previously existing
     *              keys together with the new ones contained in 'diagnosisKeys'.
     */
    void addDiagnosisKeys(String token, List<DiagnosisKey> diagnosisKeys);

    void updateDiagnosisKeys(String token, List<DiagnosisKey> diagnosisKeys);

    /**
     * @param token A token with that an addDiagnosisKeys(...) call has been performed previously
     * @throws StorageException if the token is unknown to the db
     */
    List<DiagnosisKey> getDiagnosisKeys(String token) throws StorageException;

    /**
     * Adds a set of ExposureInfomation items as well as a a single ExposureSummary
     * itemto the database. If there already is data for the given token, the data
     * will be overwritten.
     * @param token A token to identify the data later on.
     * @param exposureInformations the information ExposureInfomation objects to store
     * @param exposureSummary the ExposureSummary object to store
     */
    void putExposureMatchingResults(
        String token,
        List<ExposureInformation> exposureInformations,
        ExposureSummary exposureSummary
    );

    /**
     * Get an exposure summary object that has previously been stored using
     * 'putExposureMatchingResults(...)'. If there is no exposure summary, or it is not up-to-date
     * (i.e. further diagnosis keys have been added for the token without adding new exposure
     * matching results afterwards), an exception is thrown.
     * @param token to identify the ExposureSummary object
     * @throws StorageException if there is no ExposureSummary for that token, which is up-to-date
     */
    ExposureSummary getExposureSummary(String token) throws StorageException;

    /**
     * Get exposure information objects that have previously been stored using
     * 'putExposureMatchingResults(...)'. If there is no such data, or it is not up-to-date
     * (i.e. further diagnosis keys have been added for the token without adding new exposure
     * matching results afterwards), an exception is thrown.
     * @param token to identify the set of ExposureInformation objects
     * @throws StorageException if there is no matchin result data for that token, which is up-to-date
     */
    List<ExposureInformation> getExposureInformation(String token) throws StorageException;

    /**
     * @param token A token with that an addDiagnosisKeys(...) call has been performed previously
     * @throws StorageException if the token is unknown to the db
     */
    void deleteTokenWithData(String token) throws StorageException;
    // TODO also delete computed exposure information and summary
    // TODO when is this method actually called?



    void truncateLast14Days();

    void clearAllData();
}
