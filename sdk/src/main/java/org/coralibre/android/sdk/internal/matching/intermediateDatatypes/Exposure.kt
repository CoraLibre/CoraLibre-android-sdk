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
package org.coralibre.android.sdk.internal.matching.intermediateDatatypes

/**
 * 1 or more consecutive matches for the same rpik build an exposure
 *
 * "Exposures are defined as consecutive sightings where the time between each sighting is not
 * longer than {@link TracingParams#maxInterpolationDuration()} and the exposure duration is not
 * shorter than {@link TracingParams#minExposureBucketizedDuration()}."
 * See lines 151ff. at:
 * https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/KeyExposureEvaluator.java
 *
 * @param millisSinceEpoch The day that the interaction occurred.
 * @param attenuationValue  The time-weighted average of the attenuation.
 * @param transmissionRiskLevel A transmission risk value.
 * @param riskScore The total risk calculated for the exposure, represented as an integer between 0 and 4096, inclusive.
 *  Note that if the totalRiskScore is less than the ExposureConfiguration's minimumRiskScore, then totalRiskScore is zero.
 *
 */
data class Exposure(
    // We store some information required to build ExposureInformation objects:
    // (The comments attached to the fields are taken from https://developers.google.com/android/exposure-notifications/exposure-notifications-api )
    val millisSinceEpoch: Long,
    val durationSeconds: Long,
    val daysSinceExposure: Int,
    val attenuationValue: Int,
    val transmissionRiskLevel: Int,
    val riskScore: Int,
    val secondsBelowLowThreshold: Int,
    val secondsBetweenThresholds: Int,
    val secondsAboveHighThreshold: Int
)
