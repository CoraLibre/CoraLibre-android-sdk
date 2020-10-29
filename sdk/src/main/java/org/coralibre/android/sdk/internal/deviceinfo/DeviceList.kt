package org.coralibre.android.sdk.internal.deviceinfo

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.annotation.VisibleForTesting
import org.coralibre.android.sdk.BuildConfig
import org.coralibre.android.sdk.R
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

class DeviceList(context: Context, sharedPrefs: SharedPreferences) {
    val deviceList: Array<DeviceInfo>
    val sharedPreferences: SharedPreferences

    init {
        deviceList = loadDeviceInfoTable(context)
        sharedPreferences = sharedPrefs
    }

    class FindingsResult(
        manufacturerFindings: List<DeviceInfo>,
        oemDeviceFindings: List<DeviceInfo>,
        oemModelFindings: List<DeviceInfo>
    ) {
        @JvmField
        val manufacturerFindings: List<DeviceInfo> =
            Collections.unmodifiableList(manufacturerFindings)

        @JvmField
        val oemDeviceFindings: List<DeviceInfo> = Collections.unmodifiableList(oemDeviceFindings)

        @JvmField
        val oemModelFindings: List<DeviceInfo> = Collections.unmodifiableList(oemModelFindings)
    }

    /**
     * Tries to find the exact device in the list based on the given parameters. If the device
     * can not be found it will return the default device info, which is an average over the whole list.
     * @param manufacturer oem/manufacturer of the device
     * @param device name of the device
     * @param model name of the specific model
     * @return the exact device info from the list or the default device info
     */
    fun getExactInfo(manufacturer: String, device: String, model: String): DeviceInfo {
        val index = Arrays.binarySearch(
            deviceList,
            DeviceInfo(manufacturer, device, model, 0, 0, ConfidenceLevel.NONE)
        )
        // TODO: check if 0 < index is correct
        return if (0 < index && deviceList[index].model == model) {
            deviceList[index]
        } else {
            DeviceInfo(
                manufacturer, device, model,
                DEFAULT_INFO.rssiCorrection,
                DEFAULT_INFO.tx,
                DEFAULT_INFO.calibrationConfidence
            )
        }
    }

    private fun findingsInList(
        manufacturer: String,
        device: String,
        model: String
    ): FindingsResult {
        val manufacturerFindings = ArrayList<DeviceInfo>()
        val oemDeviceFindings = ArrayList<DeviceInfo>()
        val oemModelFindings = ArrayList<DeviceInfo>()
        for (i in deviceList) {
            if (manufacturer.equals(i.manufacturer, ignoreCase = true)) {
                manufacturerFindings.add(i)
                if (device.equals(i.device, true)) {
                    oemDeviceFindings.add(i)
                }
                if (model.equals(i.model, true)) {
                    oemModelFindings.add(i)
                }
            }
        }
        return FindingsResult(manufacturerFindings, oemDeviceFindings, oemModelFindings)
    }

    /**
     * Tries to calculate an accurate average based on similar manufacturer device or model.
     * This method tries to accomplish this by going through several steps. If one step
     * did not yield useful information the next step will be taken. The further down these
     * steps go the less accurate the result will be:
     * - If there is a full match on oem/device/model, take that
     * - If there is exactly one match with same oem/device or same oem/model, take that
     * - If there matches with same oem/device or same oem/model and confidence > LOW, take the average of them and set confidence to LOW
     * - If there are matches with same oem and confidence > LOW, take the average of them and set confidence to LOW
     * - Fallback should better not be zero, but more like industry average: RSSI: -3, TX: -25 (and confidence to LOWEST)
     *
     * Before using this method the getExactDevice() method should be called.
     * @param manufacturer oem/manufacturer of the device
     * @param device name of the device
     * @param model name of the specific model
     * @return a DeviceInfo calculated by the steps described earlier
     */
    private fun getBestFittingInfoFromList(
        manufacturer: String,
        device: String,
        model: String
    ): DeviceInfo {
        // try to find oem/device oem/model match
        val findings = findingsInList(manufacturer, device, model)
        if (findings.oemDeviceFindings.size == 1) {
            return findings.oemDeviceFindings[0]
        }
        if (findings.oemModelFindings.size == 1) {
            return findings.oemModelFindings[0]
        }
        if (findings.oemDeviceFindings.size > 1) {
            val info = getAverageOfFindings(findings.oemDeviceFindings)
            if (info.calibrationConfidence === ConfidenceLevel.LOW) {
                return info
            }
        }
        if (findings.oemModelFindings.size > 1) {
            val info = getAverageOfFindings(findings.oemModelFindings)
            if (info.calibrationConfidence === ConfidenceLevel.LOW) {
                return info
            }
        }
        return if (findings.manufacturerFindings.size > 1) {
            getAverageOfFindings(findings.manufacturerFindings)
        } else DeviceInfo(
            manufacturer,
            device,
            model,
            DEFAULT_INFO.rssiCorrection,
            DEFAULT_INFO.tx,
            ConfidenceLevel.NONE
        )
    }

    private fun getOwnDeviceInfo(
        manufacturer: String,
        device: String,
        model: String
    ): DeviceInfo {
        if (sharedPreferences.contains(DEVICE_INFO_KEY)) {
            val deviceInfo =
                DeviceInfo(sharedPreferences.getString(DEVICE_INFO_KEY, DEFAULT_INFO.toString())!!)
            if (BuildConfig.DEBUG) Log.d(
                TAG,
                "get from shared prefs: " +
                    deviceInfo.toString()
            )
            return deviceInfo
        }
        var deviceInfo = getExactInfo(manufacturer, device, model)
        if (deviceInfo.calibrationConfidence === ConfidenceLevel.NONE) {
            deviceInfo = getBestFittingInfoFromList(manufacturer, device, model)
        }
        sharedPreferences.edit().putString(DEVICE_INFO_KEY, deviceInfo.toString()).apply()
        if (BuildConfig.DEBUG) Log.d(TAG, "get from list: " + deviceInfo.toString())
        return deviceInfo
    }

    companion object {
        private val TAG = DeviceInfo::class.java.toString()
        const val DEVICE_INFO_KEY = "DeviceList_device_info_key"
        private val LIST_FILE_ID = R.raw.en_calibration_2020_08_12
        private const val LIST_LINE_COUNT = 11810

        @JvmField
        val DEFAULT_INFO = DeviceInfo(
            "unknown", "unknown", "unknown", 4, -25, ConfidenceLevel.NONE
        )

        @JvmStatic
        @Throws(IOException::class)
        fun loadDeviceInfoTable(context: Context): Array<DeviceInfo> {
            InputStreamReader(
                context.resources.openRawResource(LIST_FILE_ID)
            ).buffered().use { infoCVS ->
                // discard the first line as it just contains the header
                infoCVS.readLine()

                // TODO: this is very unsafe regarding line count changes
                return Array(LIST_LINE_COUNT - 1) {
                    val line = infoCVS.readLine()
                    DeviceInfo(line)
                }
            }
        }

        // TODO make internal
        @VisibleForTesting
        @JvmStatic
        fun getAverageOfFindings(findings: List<DeviceInfo>): DeviceInfo {
            var allConfidenceGreaterLow = true
            var rssiSum = 0
            var txSum = 0
            for (i in findings) {
                rssiSum += i.rssiCorrection
                txSum += i.tx
                if (i.calibrationConfidence === ConfidenceLevel.LOW) allConfidenceGreaterLow = false
            }
            return if (findings.isEmpty()) DeviceInfo(
                Build.MANUFACTURER,
                Build.DEVICE,
                Build.MODEL,
                0,
                0,
                ConfidenceLevel.NONE
            ) else DeviceInfo(
                Build.MANUFACTURER, Build.DEVICE, Build.MODEL,
                rssiSum / findings.size,
                txSum / findings.size,
                if (allConfidenceGreaterLow) ConfidenceLevel.LOW else ConfidenceLevel.NONE
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getOwnDeviceInfo(context: Context): DeviceInfo? {
            return DeviceList(context, PreferenceManager.getDefaultSharedPreferences(context))
                .getOwnDeviceInfo(Build.MANUFACTURER, Build.DEVICE, Build.MODEL)
        }
    }
}
