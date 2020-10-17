package org.coralibre.android.sdk.internal.matching;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.internal.EnFrameworkConstants;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;



/**
 * 1 or more consecutive matches for the same rpik build an exposure
 */
public class Exposure {
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
