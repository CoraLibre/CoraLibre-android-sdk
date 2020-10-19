package org.coralibre.android.sdk.internal.matching;

import android.util.Log;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.internal.crypto.CryptoModule;
import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.database.DatabaseAccess;
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadataKey;
import org.coralibre.android.sdk.internal.datatypes.AssociatedMetadata;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifierKey;
import org.coralibre.android.sdk.internal.device_info.DeviceInfo;
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Exposure;
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Match;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MatchingHelper {

    // TODO add more logs for debugging

    public final static String TAG = MatchingHelper.class.getSimpleName();


    /**
     * For all intervals for that captured data is present, an rpi is computed per diagnosis key,
     * then rpi matches are searched in the recorded data for that interval.
     * @param token identifies the set of diagnosis keys, for which matches are searched
     * @return true, iff at least one payload with a matching rpi has been found
     */
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
                    }
                }
            }
        }

        return false;
    }





    public static AllExposureInfo assembleAllExposureInfo(
        final String token,
        final ExposureConfiguration exposureConfiguration,
        final DeviceInfo ownDeviceInfo
    ) {
        // TODO call this method and store the results in db
        // TODO refactor: split this method

        final Database db = DatabaseAccess.getDefaultDatabaseInstance();
        final List<DiagnosisKey> diagnosisKeys = db.getDiagnosisKeys(token);

        // First, collect all matches per rpik and put them into a list. The lists are stored in a
        // map and accessible by the respective rpik:
        final HashMap<RollingProximityIdentifierKey, LinkedList<Match>> matchesByRpik = new HashMap<>();
        final HashMap<RollingProximityIdentifierKey, DiagnosisKey> diagKeysByRpik = new HashMap<>();

        final Iterable<IntervalOfCapturedData> payloadIntevals = db.getAllCollectedPayload();
        for (DiagnosisKey diagKey : diagnosisKeys) {
            RollingProximityIdentifierKey rpik = CryptoModule.generateRPIK(diagKey.getKeyData());
            diagKeysByRpik.put(rpik, diagKey);
            for (IntervalOfCapturedData interval : payloadIntevals) {
                RollingProximityIdentifier rpi = CryptoModule.generateRPI(rpik, interval.getInterval());

                for (CapturedData capturedData : interval.getCapturedData()) {
                    if (capturedData.getRpi().equals(rpi)) {

                        AssociatedMetadata metadata = CryptoModule.decryptAEM(
                            capturedData.getAem(),
                            rpi,
                            new AssociatedEncryptedMetadataKey(diagKey.getKeyData())
                        );

                        // Build a Match object; Match objects are collected in a list per rpik
                        Match match = new Match(
                            rpik, metadata, capturedData.getCaptureTimestampMillis(), capturedData.getRssi());
                        if (!matchesByRpik.containsKey(rpik)) {
                            matchesByRpik.put(rpik, new LinkedList<>());
                        }
                        matchesByRpik.get(rpik).add(match);
                    }
                }
            }
        }


        // Now, combine the different matches for each rpik to Exposure objects. Per rpik, multiple
        // Exposure object might be created:
        List<Exposure> allExposures = new LinkedList<>();
        for (RollingProximityIdentifierKey rpik : matchesByRpik.keySet()) {
            final List<Exposure> exposures = ExposureUtils.exposuresFromMatches(
                matchesByRpik.get(rpik),
                exposureConfiguration,
                diagKeysByRpik.get(rpik),
                ownDeviceInfo
            );
            allExposures.addAll(exposures);
        }


        // Finally build ExposureInformation objects as well as an ExposureSummary, which are
        // then returned:

        // ... Prepare temporary variables to collect values used for building the exposure summary:
        // Note:
        // 'If a match occurs and the exposure's calculated risk score is less than or equal to the
        // minimumRiskScore, the exposure's totalRiskScore is set to 0. Note that for the purposes
        // of calculating ExposureSummary aggregate functions, the exposure is still considered a
        // "match," and so is still included in the calculations of daysSinceLastExposure,
        // matchedKeyCount, and so on.'
        // https://developers.google.com/android/exposure-notifications/exposure-notifications-api
        // TODO verify this note has been regarded!

        int maximumRiskScore = 0;
        int summationRiskScore = 0;
        int daysSinceLastExposure = -1;
        int[] accumulatedAttenuationDurationsMinutes = new int[3]; // TODO
        List<ExposureInformation> allExposureInformations = new LinkedList<>();
        for (Exposure exposure : allExposures) {

            // First the ExposureInformation for this specific exposure:

            int[] attenuationDurationsMinutes = {
                exposure.secondsBelowLowThreshold / 60,
                exposure.secondsBetweenThresholds / 60,
                exposure.secondsAboveHighThreshold / 60
            };
            allExposureInformations.add(
                new ExposureInformation.ExposureInformationBuilder()
                    .setDateMillisSinceEpoch(exposure.millisSinceEpoch)
                    .setDurationMinutes((int)(exposure.durationSeconds / 60))
                    .setAttenuationValue(exposure.attenuationValue)
                    .setTransmissionRiskLevel(exposure.transmissionRiskLevel)
                    .setTotalRiskScore(exposure.riskScore)
                    .setAttenuationDurations(attenuationDurationsMinutes)
                    .build()
            );

            // Then this exposure is used to update the variables from that the overall
            // ExposureSummary will be built:

            // Note: In the google sample code, low scores are ignored when calculating sum and
            // maximum score, BUT that code already implements the new ExposureWindow mode:
            //
            //     if (windowScore < config.getMinimumWindowScore()) {
            //         Log.log(...)
            //         continue;
            //     }
            //     ...  -> here sum and max. score are calculated
            //
            // See line 100ff. in:
            // https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/DailySummaryUtils.java
            // TODO verify this note has been regarded! Move it to the correct place

            if (daysSinceLastExposure == -1) {
                daysSinceLastExposure = exposure.daysSinceExposure;
            } else {
                if (exposure.daysSinceExposure < daysSinceLastExposure) {
                    daysSinceLastExposure = exposure.daysSinceExposure;
                }
            }

            if (exposure.riskScore > maximumRiskScore) {
                maximumRiskScore = exposure.riskScore;
            }

            summationRiskScore += exposure.riskScore;
            // For the same rpik, but different exposures, risk scores are still summed up.
            // See lines 200ff. in:
            // https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java

            accumulatedAttenuationDurationsMinutes[0] += attenuationDurationsMinutes[0];
            accumulatedAttenuationDurationsMinutes[1] += attenuationDurationsMinutes[1];
            accumulatedAttenuationDurationsMinutes[2] += attenuationDurationsMinutes[2];
        }


        if (daysSinceLastExposure < 0) {
            Log.e(MatchingHelper.TAG, "Invalid value for daysSinceLastExposures: " + daysSinceLastExposure);
        }


        ExposureSummary exposureSummary = new ExposureSummary.ExposureSummaryBuilder()
            .setDaysSinceLastExposure(daysSinceLastExposure)
            .setMaximumRiskScore(maximumRiskScore)
            .setSummationRiskScore(summationRiskScore)
            .setMatchedKeyCount(matchesByRpik.keySet().size())
            .setAttenuationDurations(accumulatedAttenuationDurationsMinutes)
            .build();

        return new AllExposureInfo(allExposureInformations, exposureSummary);
    }


}
