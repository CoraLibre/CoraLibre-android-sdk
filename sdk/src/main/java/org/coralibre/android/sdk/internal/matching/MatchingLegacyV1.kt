package org.coralibre.android.sdk.internal.matching

import android.util.Log
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureInformation.ExposureInformationBuilder
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary.ExposureSummaryBuilder
import org.coralibre.android.sdk.internal.crypto.CryptoModule
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadataKey
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifierKey
import org.coralibre.android.sdk.internal.device_info.DeviceInfo
import org.coralibre.android.sdk.internal.matching.ExposureUtils.exposuresFromMatches
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Exposure
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.Match
import java.util.HashMap
import java.util.LinkedList

/**
 * Provides static methods to check if matches exist and to compute ExposureSummary/ExposureInformation
 * objects.
 * This class and all calculations it performs should not access the database, but receive all
 * data required for the computations as (method) parameters.
 */
object MatchingLegacyV1 {
    // TODO add more logs for debugging
    val TAG = MatchingLegacyV1::class.java.simpleName

    /**
     * Computes the rpik for each diagnosis key and search all payloads in the given intervals
     * for matches
     * @return true, iff at least one payload with a matching rpi has been found
     */
    @JvmStatic
    fun hasMatches(
        diagnosisKeys: List<DiagnosisKey>,
        payloadIntervals: Iterable<IntervalOfCapturedData>
    ): Boolean {
        for (diagKey in diagnosisKeys) {
            val rpik = CryptoModule.generateRPIK(diagKey.keyData)
            for (interval in payloadIntervals) {
                val rpi = CryptoModule.generateRPI(rpik, interval.interval)
                for (capturedData in interval.capturedData) {
                    if (capturedData.rpi == rpi) {
                        // Match found:
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Computes an ExposureSummary and multiple ExposureInformation objects, which are wrapped into
     * an AllExposureInfo object and returned.
     * @param diagnosisKeys the set of all infected diagnosis keys used for the calculation
     * @param payloadIntevals the collected bluetooth payloads used for the calculation
     * @param exposureConfiguration used for the risk value computation of single matches/exposures
     * @param ownDeviceInfo device info to compute the bluetooth attenuation value
     * @return
     */
    fun assembleAllExposureInfo(
        diagnosisKeys: List<DiagnosisKey>,
        payloadIntevals: Iterable<IntervalOfCapturedData>,
        exposureConfiguration: ExposureConfiguration,
        ownDeviceInfo: DeviceInfo
    ): AllExposureInfo {
        // TODO call this method and store the results in db
        // TODO refactor: split this method

        // First, collect all matches per rpik and put them into a list. The lists are stored in a
        // map and accessible by the respective rpik:
        val matchesByRpik = HashMap<RollingProximityIdentifierKey, LinkedList<Match>>()
        val diagKeysByRpik = HashMap<RollingProximityIdentifierKey, DiagnosisKey>()
        for (diagKey in diagnosisKeys) {
            val rpik = CryptoModule.generateRPIK(diagKey.keyData)
            diagKeysByRpik[rpik] = diagKey
            for (interval in payloadIntevals) {
                val rpi = CryptoModule.generateRPI(rpik, interval.interval)
                for (capturedData in interval.capturedData) {
                    if (capturedData.rpi == rpi) {
                        val metadata = CryptoModule.decryptAEM(
                            capturedData.aem,
                            rpi,
                            AssociatedEncryptedMetadataKey(diagKey.keyData)
                        )

                        // Build a Match object; Match objects are collected in a list per rpik
                        val match = Match(
                            rpik, metadata, capturedData.captureTimestampMillis, capturedData.rssi
                        )
                        if (!matchesByRpik.containsKey(rpik)) {
                            matchesByRpik[rpik] = LinkedList()
                        }
                        matchesByRpik[rpik]!!.add(match)
                    }
                }
            }
        }


        // Now, combine the different matches for each rpik to Exposure objects. Per rpik, multiple
        // Exposure object might be created:
        val allExposures: MutableList<Exposure> = LinkedList()
        for (rpik in matchesByRpik.keys) {
            val exposures = exposuresFromMatches(
                matchesByRpik[rpik]!!,
                exposureConfiguration,
                diagKeysByRpik[rpik]!!,
                ownDeviceInfo
            )
            allExposures.addAll(exposures)
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

        var maximumRiskScore = 0
        var summationRiskScore = 0
        var daysSinceLastExposure = -1
        // TODO
        val accumulatedAttenuationDurationsMinutes = IntArray(3)
        val allExposureInformations: MutableList<ExposureInformation> = LinkedList()
        for ((millisSinceEpoch, durationSeconds, daysSinceExposure, attenuationValue, transmissionRiskLevel, riskScore, secondsBelowLowThreshold, secondsBetweenThresholds, secondsAboveHighThreshold) in allExposures) {

            // First the ExposureInformation for this specific exposure:
            val attenuationDurationsMinutes = intArrayOf(
                secondsBelowLowThreshold / 60,
                secondsBetweenThresholds / 60,
                secondsAboveHighThreshold / 60
            )
            allExposureInformations.add(
                ExposureInformationBuilder()
                    .setDateMillisSinceEpoch(millisSinceEpoch)
                    .setDurationMinutes((durationSeconds / 60).toInt())
                    .setAttenuationValue(attenuationValue)
                    .setTransmissionRiskLevel(transmissionRiskLevel)
                    .setTotalRiskScore(riskScore)
                    .setAttenuationDurations(attenuationDurationsMinutes)
                    .build()
            )

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
                daysSinceLastExposure = daysSinceExposure
            } else {
                if (daysSinceExposure < daysSinceLastExposure) {
                    daysSinceLastExposure = daysSinceExposure
                }
            }
            if (riskScore > maximumRiskScore) {
                maximumRiskScore = riskScore
            }
            summationRiskScore += riskScore
            // For the same rpik, but different exposures, risk scores are still summed up.
            // See lines 200ff. in:
            // https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java

            accumulatedAttenuationDurationsMinutes[0] += attenuationDurationsMinutes[0]
            accumulatedAttenuationDurationsMinutes[1] += attenuationDurationsMinutes[1]
            accumulatedAttenuationDurationsMinutes[2] += attenuationDurationsMinutes[2]
        }


        if (daysSinceLastExposure < 0) {
            Log.e(TAG, "Invalid value for daysSinceLastExposures: $daysSinceLastExposure")
        }


        val exposureSummary = ExposureSummaryBuilder()
            .setDaysSinceLastExposure(daysSinceLastExposure)
            .setMaximumRiskScore(maximumRiskScore)
            .setSummationRiskScore(summationRiskScore)
            .setMatchedKeyCount(matchesByRpik.keys.size)
            .setAttenuationDurations(accumulatedAttenuationDurationsMinutes)
            .build()
        return AllExposureInfo(allExposureInformations, exposureSummary)
    }
}
