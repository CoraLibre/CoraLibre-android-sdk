package org.coralibre.android.sdk.internal.deviceinfo

import org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel.Companion.getConfidenceLevel

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

    override fun equals(otherInfo: Any?): Boolean {
        if (otherInfo!!.javaClass != javaClass) return false
        val otherDeviceInfo = otherInfo as DeviceInfo?
        return (otherDeviceInfo!!.manufacturer.equals(manufacturer, ignoreCase = true)
            && otherDeviceInfo.device.equals(device, ignoreCase = true)
            && otherDeviceInfo.model.equals(model, ignoreCase = true)
            && otherDeviceInfo.rssiCorrection == rssiCorrection && otherDeviceInfo.tx == tx && otherDeviceInfo.calibrationConfidence === calibrationConfidence)
    }

    // FIXME: implement hashCode

    override fun compareTo(otherInfo: DeviceInfo): Int {
        if (manufacturer.compareTo(otherInfo.manufacturer, ignoreCase = true) != 0) {
            return manufacturer.compareTo(otherInfo.manufacturer, ignoreCase = true)
        }
        if (model.compareTo(otherInfo.model, ignoreCase = true) != 0) {
            return model.compareTo(otherInfo.model, ignoreCase = true)
        }
        return if (device.compareTo(otherInfo.device, ignoreCase = true) != 0) {
            device.compareTo(otherInfo.device, ignoreCase = true)
        } else 0
    }
}
