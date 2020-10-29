/*
 * The following source code is based on:
 * https://github.com/google/exposure-notifications-internals/blob/8f751a666697c3cae0a56ae3464c2c6cbe31b69e/exposurenotification/src/main/java/com/google/samples/exposurenotification/matching/TimeAndAttenuation.java#L23
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

package org.coralibre.android.sdk.internal.matching.intermediateDatatypes;

import org.coralibre.android.sdk.internal.device_info.DeviceInfo;

public class TimeAndAttenuation {

    public final int timeSeconds;
    public final int attenuation;

    public TimeAndAttenuation(int timeSeconds, int attenuation) {
        this.timeSeconds = timeSeconds;
        this.attenuation = attenuation;
    }


    public static TimeAndAttenuation fromMatch(final Match match, DeviceInfo ownDeviceInfo) {
        int rssiMeasured = new Byte(match.rssi).intValue();
        int txPower = match.metadata.getTransmitPowerLevel();
        int rssiCorrection = ownDeviceInfo.getRssiCorrection();

        int attenuation = txPower - (rssiMeasured - rssiCorrection);
        int timeSeconds = (int)match.captureTimestampMillis * 1000;
        return new TimeAndAttenuation(
            timeSeconds,
            attenuation
        );
    }


}
