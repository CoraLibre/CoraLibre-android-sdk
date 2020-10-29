/*
 * The following source code is based on:
 * https://github.com/google/exposure-notifications-internals/blob/main/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/Period.java
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
package org.coralibre.android.sdk.internal.matching.intermediateDatatypes

import kotlin.math.roundToInt

class Period(val scan1: TimeAndAttenuation, val scan2: TimeAndAttenuation) {
    fun durationSeconds(): Int {
        return scan2.timeSeconds - scan1.timeSeconds
    }

    fun attenuationDiff(): Int {
        return scan2.attenuation - scan1.attenuation
    }

    fun calculateTimeCross(threshold: Int, interpolate: Boolean): Int {
        var timeCross = scan1.timeSeconds
        if (interpolate && attenuationDiff() != 0) {
            // calculate the time at which the interpolated attenuation equals the threshold.
            timeCross = (
                scan1.timeSeconds
                    + (threshold - scan1.attenuation)
                    / attenuationDiff().toDouble()
                    * durationSeconds()
                ).roundToInt()
        }
        return timeCross
    }
}
