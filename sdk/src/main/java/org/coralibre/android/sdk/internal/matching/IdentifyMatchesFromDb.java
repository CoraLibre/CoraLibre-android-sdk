package org.coralibre.android.sdk.internal.matching;

import android.util.Log;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary;
import org.coralibre.android.sdk.internal.EnFrameworkConstants;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class IdentifyMatchesFromDb {

    // TODO This is just a temporary glue class, which needs some refactoring after
    //  the tracing and matching works / after we have a working mvp.


    // TODO add more logs for debugging

    public final static String TAG = IdentifyMatchesFromDb.class.getSimpleName();


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


    /**
     * A match is associated/build from exactly 1 bluetooth payload
     * Each match corresponds to a single "sighting".
     * */
    private static class Match {
        /*
        * The term "sighting", which corresponds to one Match is used in:
        * https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java
        */

        public final RollingProximityIdentifierKey rpik;
        public final AssociatedMetadata metadata;
        public final long captureTimestampMillis;

        public Match(
            RollingProximityIdentifierKey rpik,
            AssociatedMetadata metadata,
            long captureTimestamp
        ) {
            this.rpik = rpik;
            this.metadata = metadata;
            this.captureTimestampMillis = captureTimestamp;
        }

    }

    /**
     * 1 or more consecutive matches for the same rpik build an exposure
     */
    private static class Exposure {
        /*
        * "Exposures are defined as consecutive sightings where the time between each sighting is not
        * longer than {@link TracingParams#maxInterpolationDuration()} and the exposure duration is not
        * shorter than {@link TracingParams#minExposureBucketizedDuration()}."
        * See lines 151ff. at:
        * https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java
        */

        public final double attenuationSum;
        public final double durationSum;
        public final int daysSinceExposure;
        public final int riskScore;

        /**
         * Collects consecutive matches from all matches for a single rpik and creates Exposure
         * objects from these blocks of consecutive matches.
         * @param matches an (unordered) list of matches for the same rpik
         * @return a list of Exposure objects for that rpik
         */
        public static List<Exposure> exposuresFromMatches(
            final LinkedList<Match> matches, final ExposureConfiguration exposureConfiguration
        ) {
            // First sort the matches by time to ease assembling of the exposures. The sorted
            // list will be in chronological order, i.e. the most recent match will be the last
            // list entry:
            final List<Match> matchesSorted = (LinkedList<Match>) matches.clone();
            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match o1, Match o2) {
                    if (o1.captureTimestampMillis > o2.captureTimestampMillis) {
                        return 1;
                    }
                    if (o1.captureTimestampMillis < o2.captureTimestampMillis) {
                        return -1;
                    }
                    return 0;
                }
            });

            // Now assemble Exposure objects from consecutive matches with a small difference in
            // the capture time:

            // ... for that, we first split the match list to get one list per exposure:
            LinkedList<LinkedList<Match>> matchesPerExposure = new LinkedList<>();
            final long lastMatchTimestampMillis = -1;
            for (Match match : matchesSorted) {
                if (lastMatchTimestampMillis == -1
                    || (match.captureTimestampMillis - lastMatchTimestampMillis) * 1000
                        > EnFrameworkConstants.MAX_EXPOSURE_INTERPOLATION_DURATION_SECONDS) {
                    matchesPerExposure.addLast(new LinkedList<Match>());
                }
                matchesPerExposure.getLast().addLast(match);
            }

            // ... and now use the split lists to create the actual Exposure objects, which then
            // are returned:
            final LinkedList<Exposure> exposures = new LinkedList<>();
            for (LinkedList<Match> exposureMatchesOrdered : matchesPerExposure) {
                exposures.addLast(Exposure.fromOrderedMatchList(exposureMatchesOrdered));
            }

            return exposures;
        }


        /**
         * @param matches all matches belonging to a single exposure, where the list is expected to
         *                be sorted by capture timestamp (ascending)
         * @return the created Exposure object
         */
        private static Exposure fromOrderedMatchList(final LinkedList<Match> matches) {
            // First add fake scans at start and end, as seen in:
            // Lines 314ff.:
            // https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java#L312
            // Also see the comment at lines 171ff. in the same source file.
            final LinkedList<Match> matchesWithStartAndEnd = (LinkedList<Match>) matches.clone();
            matchesWithStartAndEnd.addFirst(new Match(
                matches.getFirst().rpik,
                matches.getFirst().metadata,
                matches.getFirst().captureTimestampMillis - EnFrameworkConstants.MAX_EXPOSURE_INTERPOLATION_DURATION_SECONDS / 2
                ));
            matchesWithStartAndEnd.addLast(new Match(
                matches.getLast().rpik,
                matches.getLast().metadata,
                matches.getLast().captureTimestampMillis + EnFrameworkConstants.MAX_EXPOSURE_INTERPOLATION_DURATION_SECONDS / 2
            ));

            // TODO implement
            return null;
        }


        private Exposure(final ExposureConfiguration exposureConfiguration) {
            // TODO implement proper constructor
            this.attenuationSum = 0;
            this.durationSum = 0;
            this.daysSinceExposure = 0;

            this.riskScore = 0;
            // TODO use correct values for the following call:
/*            this.riskScore = exposureConfiguration.getRiskScore(
                metadata.,
                0,
                0,
                0
            );*/
        }




    }


    public static ExposureSummary buildExposureSummary(
        final String token, final ExposureConfiguration exposureConfiguration
    ) {
        // TODO refactor: split this method (perhaps split the whole class)

        final Database db = DatabaseAccess.getDefaultDatabaseInstance();
        final List<DiagnosisKey> diagnosisKeys = db.getDiagnosisKeys(token);

        // First, collect all matches per rpik and put them into a list. The lists are stored in a
        // map and accessible by the respective rpik:
        final HashMap<RollingProximityIdentifierKey, LinkedList<Match>> matchesByRpik = new HashMap<>();

        final Iterable<IntervalOfCapturedData> payloadIntevals = db.getAllCollectedPayload();
        for (DiagnosisKey diagKey : diagnosisKeys) {
            RollingProximityIdentifierKey rpik = CryptoModule.generateRPIK(diagKey.getKeyData());
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
                        Match match = new Match(rpik, metadata, capturedData.getCaptureTimestampMillis());
                        if (!matchesByRpik.containsKey(rpik)) {
                            matchesByRpik.put(rpik, new LinkedList<>());
                        }
                        matchesByRpik.get(rpik).add(match);
                    }
                }
            }
        }



        // Prepare temporary variables to collect values used for building the exposure summary:

        // Note:
        // 'If a match occurs and the exposure's calculated risk score is less than or equal to the
        // minimumRiskScore, the exposure's totalRiskScore is set to 0. Note that for the purposes
        // of calculating ExposureSummary aggregate functions, the exposure is still considered a
        // "match," and so is still included in the calculations of daysSinceLastExposure,
        // matchedKeyCount, and so on.'
        // https://developers.google.com/android/exposure-notifications/exposure-notifications-api

        int maximumRiskScore = 0;
        int summationRiskScore = 0;
        int daysSinceLastExposure = -1;
        int[] attenuationDurations; // TODO



        // Now, combine the different matches for each rpik to Exposure objects. Per rpik, one
        // Exposure object is created:
        for (RollingProximityIdentifierKey rpik : matchesByRpik.keySet()) {
            final List<Exposure> exposures =
                Exposure.exposuresFromMatches(matchesByRpik.get(rpik), exposureConfiguration);

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


            for (Exposure exposure : exposures) {
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
            }
        }


        if (daysSinceLastExposure < 0) {
            Log.e(IdentifyMatchesFromDb.TAG, "Invalid value for daysSinceLastExposures: " + daysSinceLastExposure);
        }


        return new ExposureSummary.ExposureSummaryBuilder()
            .setDaysSinceLastExposure(daysSinceLastExposure)
            .setMaximumRiskScore(maximumRiskScore)
            .setSummationRiskScore(summationRiskScore)
            .setMatchedKeyCount(matchesByRpik.keySet().size())
            .setAttenuationDurations(new int[3]) // TODO
            .build();

    }


}
