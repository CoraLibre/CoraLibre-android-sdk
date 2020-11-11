package org.coralibre.android.sdk.internal.deviceinfo

import org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel.Companion.getConfidenceLevel
import java.util.Locale

class DeviceInfo : Comparable<DeviceInfo> {
    val manufacturer: String
    val device: String
    val model: String
    val rssiCorrection: Int
    val tx: Int
    var calibrationConfidence: ConfidenceLevel
        private set

    constructor(
        manufacturer: String,
        device: String,
        model: String,
        rssiCorrection: Int,
        tx: Int,
        calibrationConfidence: ConfidenceLevel
    ) {
        this.manufacturer = manufacturer
        this.device = device
        this.model = model
        this.rssiCorrection = rssiCorrection
        this.tx = tx
        this.calibrationConfidence = calibrationConfidence
    }

    constructor(rawString: String) {
        val row = rawString.split(",".toRegex()).toTypedArray()
        manufacturer = row[0]
        device = row[1]
        model = row[2]
        rssiCorrection = row[3].toInt()
        tx = row[4].toInt()
        try {
            calibrationConfidence = getConfidenceLevel(row[5].toInt())
        } catch (e: NumberFormatException) {
            calibrationConfidence = getConfidenceLevel(row[5])
        }
    }

    override fun toString(): String {
        return String.format(
            "%s,%s,%s,%d,%d,%s",
            manufacturer, device, model, rssiCorrection, tx, calibrationConfidence
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other?.javaClass != javaClass) return false
        val otherDeviceInfo = other as DeviceInfo
        return (
            otherDeviceInfo.manufacturer.equals(manufacturer, ignoreCase = true) &&
                otherDeviceInfo.device.equals(device, ignoreCase = true) &&
                otherDeviceInfo.model.equals(model, ignoreCase = true) &&
                otherDeviceInfo.rssiCorrection == rssiCorrection &&
                otherDeviceInfo.tx == tx &&
                otherDeviceInfo.calibrationConfidence === calibrationConfidence
            )
    }

    override fun hashCode(): Int {
        val locale = Locale.ROOT
        var result = manufacturer.toLowerCase(locale).hashCode()
        result = 31 * result + device.toLowerCase(locale).hashCode()
        result = 31 * result + model.toLowerCase(locale).hashCode()
        result = 31 * result + rssiCorrection
        result = 31 * result + tx
        result = 31 * result + calibrationConfidence.hashCode()
        return result
    }

    override fun compareTo(other: DeviceInfo): Int {
        return compareBy<DeviceInfo>(
            { it.manufacturer.toLowerCase(Locale.ROOT) },
            { it.model.toLowerCase(Locale.ROOT) },
            { it.device.toLowerCase(Locale.ROOT) }
        ).compare(this, other)
    }
}
