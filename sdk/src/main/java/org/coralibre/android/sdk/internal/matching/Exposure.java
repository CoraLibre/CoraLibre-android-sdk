//
// The following source code is partially based on:
// https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java#L420
// which is licensed under Apache License, Version 2.0:
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//


package org.coralibre.android.sdk.internal.matching;

import android.util.Pair;

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration;
import org.coralibre.android.sdk.internal.EnFrameworkConstants;
import org.coralibre.android.sdk.internal.TracingService;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


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

    // We store some information required to build ExposureInformation objects:
    // (The comments attached to the fields are taken from https://developers.google.com/android/exposure-notifications/exposure-notifications-api )
    public final long millisSinceEpoch; // The day that the interaction occurred.
    public final long durationSeconds;
    public final int daysSinceExposure;
    public final int attenuationValue; // The time-weighted average of the attenuation.
    public final int transmissionRiskLevel; // A transmission risk value.
    public final int riskScore;
        // The total risk calculated for the exposure, represented as an integer between 0 and 4096, inclusive.
        // Note that if the totalRiskScore is less than the ExposureConfiguration's minimumRiskScore, then totalRiskScore is zero.
    public final int secondsBelowLowThreshold;
    public final int secondsBetweenThresholds;
    public final int secondsAboveHighThreshold;


    /**
     * Collects consecutive matches from all matches for a single rpik and creates Exposure
     * objects from these blocks of consecutive matches.
     * @param matches an (unordered) list of matches for the same rpik
     * @return a list of Exposure objects for that rpik
     */
    public static List<Exposure> exposuresFromMatches(
        final LinkedList<Match> matches, final ExposureConfiguration exposureConfiguration, DiagnosisKey diagnosisKey
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

        // Now assemble Exposure objects from consecutive matches that have only a small difference
        // in the capture time:

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
            Exposure exposure = fromOrderedMatchList(exposureMatchesOrdered, exposureConfiguration, diagnosisKey);
            if (exposure != null) {
                exposures.addLast(exposure);
            }
        }

        return exposures;
    }


    // Based on the google sample code:
    private static List<Period> computePeriods(
        LinkedList<TimeAndAttenuation> timeAndAttenuations, long scanInterval) {
        // Add fake start and end boundary scans with the same attenuation as first and last ones.
        int timeMargin = (int) (TracingService.SCAN_INTERVAL_MILLIS / 2);
        timeAndAttenuations.addFirst(
            new TimeAndAttenuation(
                timeAndAttenuations.getFirst().timeSeconds - timeMargin,
                timeAndAttenuations.getFirst().attenuation
            ));
        timeAndAttenuations.addLast(
            new TimeAndAttenuation(
                timeAndAttenuations.getLast().timeSeconds + timeMargin,
                timeAndAttenuations.getLast().attenuation
            ));

        List<Period> periods = new ArrayList<>();

        TimeAndAttenuation previous = timeAndAttenuations.get(0);
        for (TimeAndAttenuation timeAndAttenuation : timeAndAttenuations) {
            // Nothing to do for 0-length segments.
            if (timeAndAttenuation.timeSeconds == previous.timeSeconds) {
                continue;
            }
            periods.add(new Period(previous, timeAndAttenuation));
            previous = timeAndAttenuation;
        }

        return periods;
    }


    /**
     * Calculates a time-weighted attenuation value.
     */
    private static int weightAttenuationOfPeriods(List<Period> periods) {
        double attenuationSum = 0;
        double durationSum = 0;
        for (Period period : periods) {
            attenuationSum += period.scan1.attenuation * period.durationSeconds();
            durationSum += period.durationSeconds();
        }
        return (int) Math.round(attenuationSum / durationSum);
    }



    private static List<Integer> getTimeBelowBetweenAndAbove(
        List<Period> periods, int thresholdLow, int thresholdHigh, boolean interpolate) {
        Pair<Integer, Integer> timeBelowAndAboveOfLow =
            getTimeBelowAndAbove(periods, thresholdLow, interpolate);
        Pair<Integer, Integer> timeBelowAndAboveOfHigh =
            getTimeBelowAndAbove(periods, thresholdHigh, interpolate);

        int timeBelowLow = timeBelowAndAboveOfLow.first;
        int timeAboveHigh = timeBelowAndAboveOfHigh.second;
        int totalTime = timeBelowAndAboveOfLow.first + timeBelowAndAboveOfLow.second;
        // Written explicitly to avoid confusion.
        int timeBetween = totalTime - timeBelowLow - timeAboveHigh;

        return Arrays.asList(timeBelowLow, timeBetween, timeAboveHigh);
    }


    /**
     * Calculates duration below and above the given threshold.
     *
     * <p>Set {@code interpolate} to {@code true} to use linear interpolation of the attenuation
     * values.
     */
    private static Pair<Integer, Integer> getTimeBelowAndAbove(
        List<Period> periods, int threshold, boolean interpolate) {
        int timeAboveThreshold = 0;
        int timeBelowThreshold = 0;
        for (Period period : periods) {

            int timeCross = period.calculateTimeCross(threshold, interpolate);

            // No cross in the interval.
            if (timeCross <= period.scan1.timeSeconds || timeCross >= period.scan2.timeSeconds) {
                if (period.scan1.attenuation >= threshold) {
                    // The entire interval is above the threshold.
                    timeAboveThreshold += period.durationSeconds();
                } else {
                    // The entire interval is below threshold.
                    timeBelowThreshold += period.durationSeconds();
                }
            } else {
                // Cross in the interval, see how much is above and below.
                int leftTime = timeCross - period.scan1.timeSeconds;
                int rightTime = period.scan2.timeSeconds - timeCross;
                if (period.scan1.attenuation >= threshold) {
                    // The left hand side is above threshold
                    timeAboveThreshold += leftTime;
                    timeBelowThreshold += rightTime;
                } else {
                    // The right hand side is above the threshold
                    timeAboveThreshold += rightTime;
                    timeBelowThreshold += leftTime;
                }
            }
        }
        return new Pair<>(timeBelowThreshold, timeAboveThreshold);
    }


    // TODO understand, why this is done and verify that it has to be done
    /**
     * For every observation of a sighting (which occurs at {@code scanInterval}, our best estimate
     * for the duration of exposure is {@code scanInterval}. Therefore, to calculate exposure
     * duration, {@code scanInterval} must be added and the estimate must be rounded to the nearest
     * {@code scanInterval} increment.
     */
    private static long bucketizeDurationSeconds(long sightedScanDurationSeconds, long scanIntervalSeconds) {
        long estimatedExposureDuration = sightedScanDurationSeconds + scanIntervalSeconds;
        long mod = estimatedExposureDuration % scanIntervalSeconds;
        if (mod <= scanIntervalSeconds / 2) {
            return estimatedExposureDuration - mod;
        }
        return estimatedExposureDuration + scanIntervalSeconds - mod;
    }


    /**
     * @param matches all matches belonging to a single exposure, where the list is expected to
     *                be sorted by capture timestamp (ascending)
     * @return the created Exposure object, if the contact was enough to make for a valid exposure, null otherwise
     */
    private static Exposure fromOrderedMatchList(
        final LinkedList<Match> matches,
        final ExposureConfiguration exposureConfiguration,
        final DiagnosisKey diagnosisKey
        ) {

        final LinkedList<TimeAndAttenuation> timeAndAttenuations = new LinkedList<>();
        for (Match match : matches) {
            timeAndAttenuations.addLast(TimeAndAttenuation.fromMatch(match));
        }


        long bucketizedDurationSeconds = bucketizeDurationSeconds(
            1000 * (matches.getLast().captureTimestampMillis - matches.getFirst().captureTimestampMillis),
            TracingService.SCAN_INTERVAL_SECONDS
        );
        List<Period> periods =
            computePeriods(timeAndAttenuations, TracingService.SCAN_INTERVAL_SECONDS);
        int weightedAttenuation = weightAttenuationOfPeriods(periods);
        int durationAtAttenuationThresholdLow =
            exposureConfiguration.getDurationAtAttenuationLowThreshold();
        int durationAtAttenuationThresholdHigh =
            exposureConfiguration.getDurationAtAttenuationHighThreshold();
        List<Integer> timesBelowBetweenAndAbove =
            getTimeBelowBetweenAndAbove(
                periods,
                durationAtAttenuationThresholdLow,
                durationAtAttenuationThresholdHigh,
                EnFrameworkConstants.INTERPOLATION_ENABLED);
        // TODO Fix log message:
/*        Log.d(TAG,
            "%s Bucketed duration=%dm >= min_duration=%dm ? %b.",
                bucketizedDuration.getStandardMinutes(),
                tracingParams.minExposureBucketizedDuration().getStandardMinutes(),
                !bucketizedDuration.isShorterThan(tracingParams.minExposureBucketizedDuration()));*/
        if (bucketizedDurationSeconds >= EnFrameworkConstants.MIN_BUCKETIZED_DURATION_SECONDS) {
            // TODO Fix log message:
            //Log.log.atVerbose().log("%s Found exposure.", instanceLogTag);


            long millisSinceEpoch = diagnosisKey.getInterval().getUnixTime() * 1000;

            long timeNow = new Date().getTime();
            int daysSinceExposure = (int)MILLISECONDS.toDays(timeNow - millisSinceEpoch);

            ExposureRecord tmpExposureRecord = new ExposureRecord(
                millisSinceEpoch,
                bucketizedDurationSeconds,
                weightedAttenuation,
                diagnosisKey.getTransmissionRiskLevel(),
                daysSinceExposure
            );

            int riskScore = RiskScoreCalculator.calculateRiskScore(tmpExposureRecord, exposureConfiguration);


            Exposure exposure = new Exposure(
                millisSinceEpoch,
                bucketizedDurationSeconds,
                daysSinceExposure,
                weightedAttenuation,
                diagnosisKey.getTransmissionRiskLevel(),
                riskScore,
                timesBelowBetweenAndAbove.get(0),
                timesBelowBetweenAndAbove.get(1),
                timesBelowBetweenAndAbove.get(2)
            );

            return exposure;
        } else {
            return null;
        }
    }


    private Exposure(
        final long millisSinceEpoch, // The day that the interaction occurred.
        final long durationSeconds,
        final int daysSinceExposure,
        final int attenuationValue, // The time-weighted average of the attenuation.
        final int transmissionRiskLevel, // A transmission risk value.
        final int riskScore,
        final int secondsBelowLowThreshold,
        final int secondsBetweenThresholds,
        final int secondsAboveHighThreshold
    ) {
        this.millisSinceEpoch = millisSinceEpoch;
        this.durationSeconds = durationSeconds;
        this.daysSinceExposure = daysSinceExposure;
        this.attenuationValue = attenuationValue;
        this.transmissionRiskLevel = transmissionRiskLevel;
        this.riskScore = riskScore;
        this.secondsBelowLowThreshold = secondsBelowLowThreshold;
        this.secondsBetweenThresholds = secondsBetweenThresholds;
        this.secondsAboveHighThreshold = secondsAboveHighThreshold;
    }

}
