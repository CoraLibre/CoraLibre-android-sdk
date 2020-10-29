package org.coralibre.android.sdk.internal.matching

import android.util.Pair
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration
import org.coralibre.android.sdk.internal.EnFrameworkConstants
import org.coralibre.android.sdk.internal.TracingService
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.device_info.DeviceInfo
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Exposure
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.ExposureRecord
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Match
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Period
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.TimeAndAttenuation
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.TimeAndAttenuation.Companion.fromMatch
import java.util.ArrayList
import java.util.Date
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object ExposureUtils {
    /**
     * Collects consecutive matches from all matches for a single rpik and creates Exposure
     * objects from these blocks of consecutive matches.
     * @param matches an (unordered) list of matches for the same rpik
     * @return a list of Exposure objects for that rpik
     */
    @JvmStatic
    fun exposuresFromMatches(
        matches: LinkedList<Match>,
        exposureConfiguration: ExposureConfiguration,
        diagnosisKey: DiagnosisKey,
        ownDeviceInfo: DeviceInfo
    ): List<Exposure> {
        // First sort the matches by time to ease assembling of the exposures. The sorted
        // list will be in chronological order, i.e. the most recent match will be the last
        // list entry:
        val matchesSorted: List<Match> = matches.sortedBy { it.captureTimestampMillis }

        // Now assemble Exposure objects from consecutive matches that have only a small difference
        // in the capture time:

        // ... for that, we first split the match list to get one list per exposure:
        val matchesPerExposure = LinkedList<LinkedList<Match>>()
        val lastMatchTimestampMillis: Long = -1
        for (match in matchesSorted) {
            if (lastMatchTimestampMillis == -1L || match.captureTimestampMillis - lastMatchTimestampMillis
                > EnFrameworkConstants.MAX_EXPOSURE_INTERPOLATION_DURATION_SECONDS * 1000
            ) {
                matchesPerExposure.addLast(LinkedList())
            }
            matchesPerExposure.last.addLast(match)
        }

        // ... and now use the split lists to create the actual Exposure objects, which then
        // are returned:
        val exposures = LinkedList<Exposure>()
        for (exposureMatchesOrdered in matchesPerExposure) {
            val exposure = fromOrderedMatchList(
                exposureMatchesOrdered, exposureConfiguration, diagnosisKey, ownDeviceInfo
            )
            if (exposure != null) {
                exposures.addLast(exposure)
            }
        }
        return exposures
    }

    // Based on the google sample code:
    private fun computePeriods(
        timeAndAttenuations: LinkedList<TimeAndAttenuation>, scanInterval: Long
    ): List<Period> {
        // Add fake start and end boundary scans with the same attenuation as first and last ones.
        val timeMargin = (TracingService.SCAN_INTERVAL_MILLIS / 2).toInt()
        timeAndAttenuations.addFirst(
            TimeAndAttenuation(
                timeAndAttenuations.first.timeSeconds - timeMargin,
                timeAndAttenuations.first.attenuation
            )
        )
        timeAndAttenuations.addLast(
            TimeAndAttenuation(
                timeAndAttenuations.last.timeSeconds + timeMargin,
                timeAndAttenuations.last.attenuation
            )
        )
        val periods: MutableList<Period> = ArrayList()
        var previous = timeAndAttenuations[0]
        for (timeAndAttenuation in timeAndAttenuations) {
            // Nothing to do for 0-length segments.
            if (timeAndAttenuation.timeSeconds == previous.timeSeconds) {
                continue
            }
            periods.add(Period(previous, timeAndAttenuation))
            previous = timeAndAttenuation
        }
        return periods
    }

    /**
     * Calculates a time-weighted attenuation value.
     */
    private fun weightAttenuationOfPeriods(periods: List<Period>): Int {
        var attenuationSum = 0.0
        var durationSum = 0.0
        for (period in periods) {
            attenuationSum += period.scan1.attenuation * period.durationSeconds().toDouble()
            durationSum += period.durationSeconds().toDouble()
        }
        return (attenuationSum / durationSum).roundToInt()
    }

    private fun getTimeBelowBetweenAndAbove(
        periods: List<Period>, thresholdLow: Int, thresholdHigh: Int, interpolate: Boolean
    ): List<Int> {
        val timeBelowAndAboveOfLow = getTimeBelowAndAbove(periods, thresholdLow, interpolate)
        val timeBelowAndAboveOfHigh = getTimeBelowAndAbove(periods, thresholdHigh, interpolate)
        val timeBelowLow = timeBelowAndAboveOfLow.first
        val timeAboveHigh = timeBelowAndAboveOfHigh.second
        val totalTime = timeBelowAndAboveOfLow.first + timeBelowAndAboveOfLow.second
        // Written explicitly to avoid confusion.
        val timeBetween = totalTime - timeBelowLow - timeAboveHigh
        return listOf(timeBelowLow, timeBetween, timeAboveHigh)
    }

    /**
     * Calculates duration below and above the given threshold.
     *
     *
     * Set `interpolate` to `true` to use linear interpolation of the attenuation
     * values.
     */
    private fun getTimeBelowAndAbove(
        periods: List<Period>, threshold: Int, interpolate: Boolean
    ): Pair<Int, Int> {
        var timeAboveThreshold = 0
        var timeBelowThreshold = 0
        for (period in periods) {
            val timeCross = period.calculateTimeCross(threshold, interpolate)

            // No cross in the interval.
            if (timeCross <= period.scan1.timeSeconds || timeCross >= period.scan2.timeSeconds) {
                if (period.scan1.attenuation >= threshold) {
                    // The entire interval is above the threshold.
                    timeAboveThreshold += period.durationSeconds()
                } else {
                    // The entire interval is below threshold.
                    timeBelowThreshold += period.durationSeconds()
                }
            } else {
                // Cross in the interval, see how much is above and below.
                val leftTime = timeCross - period.scan1.timeSeconds
                val rightTime = period.scan2.timeSeconds - timeCross
                if (period.scan1.attenuation >= threshold) {
                    // The left hand side is above threshold
                    timeAboveThreshold += leftTime
                    timeBelowThreshold += rightTime
                } else {
                    // The right hand side is above the threshold
                    timeAboveThreshold += rightTime
                    timeBelowThreshold += leftTime
                }
            }
        }
        return Pair(timeBelowThreshold, timeAboveThreshold)
    }
    // TODO understand, why this is done and verify that it has to be done
    /**
     * For every observation of a sighting (which occurs at `scanInterval`, our best estimate
     * for the duration of exposure is `scanInterval`. Therefore, to calculate exposure
     * duration, `scanInterval` must be added and the estimate must be rounded to the nearest
     * `scanInterval` increment.
     */
    private fun bucketizeDurationSeconds(
        sightedScanDurationSeconds: Long,
        scanIntervalSeconds: Long
    ): Long {
        val estimatedExposureDuration = sightedScanDurationSeconds + scanIntervalSeconds
        val mod = estimatedExposureDuration % scanIntervalSeconds
        return if (mod <= scanIntervalSeconds / 2) {
            estimatedExposureDuration - mod
        } else estimatedExposureDuration + scanIntervalSeconds - mod
    }

    /**
     * @param matches all matches belonging to a single exposure, where the list is expected to
     * be sorted by capture timestamp (ascending)
     * @return the created Exposure object, if the contact was enough to make for a valid exposure, null otherwise
     */
    private fun fromOrderedMatchList(
        matches: LinkedList<Match>,
        exposureConfiguration: ExposureConfiguration,
        diagnosisKey: DiagnosisKey,
        ownDeviceInfo: DeviceInfo
    ): Exposure? {
        val timeAndAttenuations = LinkedList<TimeAndAttenuation>()
        for (match in matches) {
            timeAndAttenuations.addLast(fromMatch(match, ownDeviceInfo))
        }
        val bucketizedDurationSeconds = bucketizeDurationSeconds(
            1000 * (matches.last.captureTimestampMillis - matches.first.captureTimestampMillis),
            TracingService.SCAN_INTERVAL_SECONDS
        )
        val periods = computePeriods(timeAndAttenuations, TracingService.SCAN_INTERVAL_SECONDS)
        val weightedAttenuation = weightAttenuationOfPeriods(periods)
        val durationAtAttenuationThresholdLow =
            exposureConfiguration.durationAtAttenuationLowThreshold
        val durationAtAttenuationThresholdHigh =
            exposureConfiguration.durationAtAttenuationHighThreshold
        val timesBelowBetweenAndAbove = getTimeBelowBetweenAndAbove(
            periods,
            durationAtAttenuationThresholdLow,
            durationAtAttenuationThresholdHigh,
            EnFrameworkConstants.INTERPOLATION_ENABLED
        )
        // TODO Fix log message:
        /*
            Log.d(TAG,
            "%s Bucketed duration=%dm >= min_duration=%dm ? %b.",
                bucketizedDuration.getStandardMinutes(),
                tracingParams.minExposureBucketizedDuration().getStandardMinutes(),
                !bucketizedDuration.isShorterThan(tracingParams.minExposureBucketizedDuration()));
        */
        if (bucketizedDurationSeconds < EnFrameworkConstants.MIN_BUCKETIZED_DURATION_SECONDS) {
            return null
        }

        // TODO Fix log message:
        //Log.log.atVerbose().log("%s Found exposure.", instanceLogTag);
        val millisSinceEpoch = diagnosisKey.interval.unixTime * 1000
        val timeNow = Date().time
        val daysSinceExposure = TimeUnit.MILLISECONDS.toDays(timeNow - millisSinceEpoch)
            .toInt()
        val tmpExposureRecord = ExposureRecord(
            millisSinceEpoch,
            bucketizedDurationSeconds,
            weightedAttenuation,
            diagnosisKey.transmissionRiskLevel,
            daysSinceExposure
        )
        val riskScore =
            RiskScoreCalculator.calculateRiskScore(tmpExposureRecord, exposureConfiguration)
        return Exposure(
            millisSinceEpoch,
            bucketizedDurationSeconds,
            daysSinceExposure,
            weightedAttenuation,
            diagnosisKey.transmissionRiskLevel,
            riskScore,
            timesBelowBetweenAndAbove[0],
            timesBelowBetweenAndAbove[1],
            timesBelowBetweenAndAbove[2]
        )
    }
}
