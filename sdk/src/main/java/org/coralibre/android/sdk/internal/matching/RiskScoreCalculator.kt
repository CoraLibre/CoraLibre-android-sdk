/*
 * The following source code is based on:
 * https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/RiskScoreCalculator.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.coralibre.android.sdk.internal.matching

import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration
import org.coralibre.android.sdk.internal.matching.intermediateDatatypes.ExposureRecord
import java.util.concurrent.TimeUnit

/**
 * Used to calculate risk score based on client configuration and exposures.
 */
object RiskScoreCalculator {
    /**
     * Returns a V1 risk score. Returns 0 if calculated risk score is below [ ][ExposureConfiguration.getMinimumRiskScore]. Throws IllegalArgumentException on invalid input.
     */
    @JvmStatic
    internal fun calculateRiskScore(
        exposureRecord: ExposureRecord,
        configuration: ExposureConfiguration
    ): Int {
        val riskScore = configuration.getRiskScore(
            exposureRecord.attenuationValue,
            exposureRecord.daysSinceExposure,
            TimeUnit.SECONDS.toMinutes(exposureRecord.durationSeconds).toInt(),
            exposureRecord.transmissionRiskLevel
        )
        return if (riskScore < configuration.minimumRiskScore) 0
        else riskScore
    }
}
