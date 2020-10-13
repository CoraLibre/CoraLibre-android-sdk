package org.coralibre.android.sdk.fakegms.nearby;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.internal.crypto.AssociatedEncryptedMetadata;
import org.coralibre.android.sdk.internal.crypto.AssociatedEncryptedMetadataKey;
import org.coralibre.android.sdk.internal.crypto.AssociatedMetadata;
import org.coralibre.android.sdk.internal.crypto.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.crypto.RollingProximityIdentifierKey;
import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.database.DatabaseAccess;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;

import java.util.List;

public class IdentifyMatchesFromDb {

    // This is just a temporary glue class, which needs some refactoring after
    // the tracing and matching works / after we have a working mvp.
    // There is much redundant code here!


    public static boolean hasMatches(String token) {

        Database db = DatabaseAccess.getDefaultDatabaseInstance();
        List<DiagnosisKey> diagnosisKeys = db.getDiagnosisKeys(token);
        Iterable<IntervalOfCapturedData> payloadIntevals = db.getAllCollectedPayload();

        for (DiagnosisKey diagKey : diagnosisKeys) {
            RollingProximityIdentifierKey rpik = CryptoModule.generateRPIK(diagKey.getKeyData());
            for (IntervalOfCapturedData interval : payloadIntevals) {
                RollingProximityIdentifier rpi = CryptoModule.generateRPI(rpik, interval.getInterval());

                for (CapturedData capturedData : interval.getCapturedData()) {
                    if (capturedData.getRpi().equals(rpi)) {
                        // Match found:

                        return true;

                        // TODO use the token to store the results
                    }
                }
            }
        }

        return false;
    }


    public static ExposureSummary buildExposureSummaryFromMatches(String token) {

        int maximumRiskScore = 0;
        int summationRiskScore = 0;
        int matchedKeyCount = 0;
        int daysSinceLastExposure = -1; // TODO compute from diagnosis key intervals
        int[] attenuationDurations; // TODO

        Database db = DatabaseAccess.getDefaultDatabaseInstance();
        List<DiagnosisKey> diagnosisKeys = db.getDiagnosisKeys(token);
        Iterable<IntervalOfCapturedData> payloadIntevals = db.getAllCollectedPayload();

        for (DiagnosisKey diagKey : diagnosisKeys) {
            boolean hasMatchForThisKey = false;

            RollingProximityIdentifierKey rpik = CryptoModule.generateRPIK(diagKey.getKeyData());
            for (IntervalOfCapturedData interval : payloadIntevals) {
                RollingProximityIdentifier rpi = CryptoModule.generateRPI(rpik, interval.getInterval());

                for (CapturedData capturedData : interval.getCapturedData()) {
                    if (capturedData.getRpi().equals(rpi)) {
                        // Match found:
                        hasMatchForThisKey = true;

                        AssociatedMetadata metadata = CryptoModule.decryptAEM(
                            new AssociatedEncryptedMetadata(capturedData.getAem()),
                            rpi,
                            new AssociatedEncryptedMetadataKey(diagKey.getKeyData())
                        );


                        int riskScore = 0; // TODO get risk score for current match

                        if (riskScore > maximumRiskScore) {
                            maximumRiskScore = riskScore;
                        }

                        summationRiskScore += riskScore;
                        // TODO check if different risk scores for the same diagnosis key should actually be summed up


                    }
                }
            }
            if (hasMatchForThisKey) {
                matchedKeyCount ++;
            }
        }

        return new ExposureSummary.ExposureSummaryBuilder()
            .setDaysSinceLastExposure(daysSinceLastExposure) // TODO calculate days
            .setMaximumRiskScore(maximumRiskScore)
            .setSummationRiskScore(summationRiskScore)
            .setMatchedKeyCount(matchedKeyCount)
            .setAttenuationDurations(new int[3]) // TODO
            .build();

    }


}
