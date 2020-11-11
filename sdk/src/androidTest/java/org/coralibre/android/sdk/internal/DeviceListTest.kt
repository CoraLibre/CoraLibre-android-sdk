package org.coralibre.android.sdk.internal

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel
import org.coralibre.android.sdk.internal.deviceinfo.DeviceInfo
import org.coralibre.android.sdk.internal.deviceinfo.DeviceList
import org.coralibre.android.sdk.internal.deviceinfo.DeviceList.Companion.getAverageOfFindings
import org.coralibre.android.sdk.internal.deviceinfo.DeviceList.Companion.loadDeviceInfoTable
import org.coralibre.android.sdk.internal.deviceinfo.DeviceList.FindingsResult
import org.junit.Assert
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class DeviceListTest {
    private lateinit var context: Context
    private lateinit var deviceList: DeviceList

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val sharedPreferences = Mockito.mock(
            SharedPreferences::class.java
        )
        val editor = Mockito.mock(
            SharedPreferences.Editor::class.java
        )
        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(editor.putString(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(editor)
        deviceList = DeviceList(context, sharedPreferences)
    }

    @Throws(Exception::class)
    private fun runFindingsInList(
        deviceList: DeviceList,
        manufacturer: String,
        device: String,
        model: String
    ): FindingsResult {
        val findingsInListMethod = DeviceList::class.java.getDeclaredMethod(
            "findingsInList",
            String::class.java, String::class.java, String::class.java
        )
        findingsInListMethod.isAccessible = true
        return findingsInListMethod.invoke(
            deviceList, manufacturer, device, model
        ) as FindingsResult
    }

    @Throws(Exception::class)
    private fun runGetAverageOfFindings(
        deviceList: DeviceList,
        findings: List<DeviceInfo>
    ): DeviceInfo {
        val getAverageOfFindingsMethod = DeviceList::class.java.getDeclaredMethod(
            "getAverageOfFindings", MutableList::class.java
        )
        getAverageOfFindingsMethod.isAccessible = true
        return getAverageOfFindingsMethod.invoke(deviceList, findings) as DeviceInfo
    }

    @Throws(Exception::class)
    private fun runGetBestFittingInfoFormList(
        deviceList: DeviceList,
        oem: String,
        device: String,
        model: String
    ): DeviceInfo {
        val getBestFittingInfoFromListMethod = DeviceList::class.java.getDeclaredMethod(
            "getBestFittingInfoFromList", String::class.java, String::class.java, String::class.java
        )
        getBestFittingInfoFromListMethod.isAccessible = true
        return getBestFittingInfoFromListMethod.invoke(deviceList, oem, device, model) as DeviceInfo
    }

    @Throws(Exception::class)
    private fun runGetOwnDeviceInfo(
        deviceList: DeviceList,
        oem: String,
        device: String,
        model: String
    ): DeviceInfo {
        val getOwnDeviceInfoMethod = DeviceList::class.java.getDeclaredMethod(
            "getOwnDeviceInfo", String::class.java, String::class.java, String::class.java
        )
        getOwnDeviceInfoMethod.isAccessible = true
        return getOwnDeviceInfoMethod.invoke(deviceList, oem, device, model) as DeviceInfo
    }

    @Test
    @Throws(Exception::class)
    fun testLoadDeviceList() {
        val deviceList = loadDeviceInfoTable(context)
        for (i in deviceList) {
            println(i)
            assertTrue("manufacturer seems to be empty", i.manufacturer.isNotEmpty())
            assertNotSame(
                "confidence may not be NONE",
                i.calibrationConfidence,
                ConfidenceLevel.NONE
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testLoadDeviceListLength() {
        val deviceList = loadDeviceInfoTable(context)
        Assert.assertEquals(11809, deviceList.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testGetInfoForKnownDevice() {
        val info = deviceList.getExactInfo("asus", "P024_1", "P024")
        assertDevice(info, "asus", "P024_1", "P024", 3, -22, ConfidenceLevel.LOW)
    }

    @Test
    @Throws(Exception::class)
    fun testGetInfoForUnknownDevice() {
        val info = deviceList.getExactInfo("testphone", "versionone", "modelone")
        assertDevice(info, "testphone", "versionone", "modelone", 4, -25, ConfidenceLevel.NONE)
    }

    @Test
    @Throws(Exception::class)
    fun testFindingsInListForUnknownSamsungZaninDevice() {
        val result = runFindingsInList(deviceList, "samsung", "zanin", "unknown")
        Assert.assertEquals(2340, result.manufacturerFindings.size.toLong())
        Assert.assertEquals(3, result.oemDeviceFindings.size.toLong())
        Assert.assertEquals(0, result.oemModelFindings.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testFindingsInListForUnknownAsusA001dModel() {
        val result = runFindingsInList(deviceList, "asus", "unknown", "ASUS_A001D")
        Assert.assertEquals(347, result.manufacturerFindings.size.toLong())
        Assert.assertEquals(0, result.oemDeviceFindings.size.toLong())
        Assert.assertEquals(2, result.oemModelFindings.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testGetAverageOfSamsungZaninDevice() {
        val findings = runFindingsInList(deviceList, "samsung", "zanin", "unknown")
        val oemAverage = getAverageOfFindings(findings.manufacturerFindings)
        val deviceAverage = getAverageOfFindings(findings.oemDeviceFindings)
        val modelAverage = getAverageOfFindings(findings.oemModelFindings)
        Assert.assertEquals(5, oemAverage.rssiCorrection.toLong())
        Assert.assertEquals(-23, oemAverage.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.NONE, oemAverage.calibrationConfidence)
        Assert.assertEquals(5, deviceAverage.rssiCorrection.toLong())
        Assert.assertEquals(-24, deviceAverage.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.NONE, deviceAverage.calibrationConfidence)
        Assert.assertEquals(0, modelAverage.rssiCorrection.toLong())
        Assert.assertEquals(0, modelAverage.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.NONE, modelAverage.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testBestFittingForOneDeviceMatch() {
        val info = runGetBestFittingInfoFormList(deviceList, "google", "blueline", "unknown")
        Assert.assertEquals(5, info.rssiCorrection.toLong())
        Assert.assertEquals(-33, info.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.HIGH, info.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testBestFittingForOneModelMatch() {
        val info = runGetBestFittingInfoFormList(deviceList, "google", "unknown", "Pixel 3")
        Assert.assertEquals(5, info.rssiCorrection.toLong())
        Assert.assertEquals(-33, info.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.HIGH, info.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testBestFittingForKnownVendor() {
        val info = runGetBestFittingInfoFormList(deviceList, "samsung", "unknown", "unknown")
        Assert.assertEquals(5, info.rssiCorrection.toLong())
        Assert.assertEquals(-23, info.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.NONE, info.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testGetOwnInfoForKnownDevice() {
        val info = runGetOwnDeviceInfo(deviceList, "google", "blueline", "Pixel 3")
        Assert.assertEquals(5, info.rssiCorrection.toLong())
        Assert.assertEquals(-33, info.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.HIGH, info.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testGetOwnInfoForUnknownDevice() {
        val info = runGetOwnDeviceInfo(deviceList, "unknown", "unknown", "unknown")
        Assert.assertEquals(4, info.rssiCorrection.toLong())
        Assert.assertEquals(-25, info.tx.toLong())
        Assert.assertEquals(ConfidenceLevel.NONE, info.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testGetOwnInfoFromSharedPreferences() {
        val prefs = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(prefs.contains(DeviceList.DEVICE_INFO_KEY)).thenReturn(true)
        Mockito.`when`(
            prefs.getString(
                DeviceList.DEVICE_INFO_KEY,
                DeviceList.DEFAULT_INFO.toString()
            )
        )
            .thenReturn(TEST_DEVICE.toString())
        val deviceList = DeviceList(context, prefs)
        val info = runGetOwnDeviceInfo(
            deviceList,
            TEST_DEVICE.manufacturer,
            TEST_DEVICE.device,
            TEST_DEVICE.model
        )
        Assert.assertEquals(TEST_DEVICE.manufacturer, info.manufacturer)
        Assert.assertEquals(TEST_DEVICE.device, info.device)
        Assert.assertEquals(TEST_DEVICE.model, info.model)
        Assert.assertEquals(TEST_DEVICE.rssiCorrection.toLong(), info.rssiCorrection.toLong())
        Assert.assertEquals(TEST_DEVICE.tx.toLong(), info.tx.toLong())
        Assert.assertEquals(TEST_DEVICE.calibrationConfidence, info.calibrationConfidence)
    }

    @Test
    @Throws(Exception::class)
    fun testGetOwnInfoFromListAndSaveItInPreferences() {
        val prefs = Mockito.mock(SharedPreferences::class.java)
        val editor = Mockito.mock(
            SharedPreferences.Editor::class.java
        )
        Mockito.`when`(prefs.contains(DeviceList.DEVICE_INFO_KEY)).thenReturn(false)
        Mockito.`when`(prefs.edit()).thenReturn(editor)
        Mockito.`when`(editor.putString(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(editor)
        val deviceList = DeviceList(context, prefs)
        val info = runGetOwnDeviceInfo(
            deviceList,
            TEST_DEVICE.manufacturer,
            TEST_DEVICE.device,
            TEST_DEVICE.model
        )
        val keyCaptor = ArgumentCaptor.forClass(
            String::class.java
        )
        val valueCaptor = ArgumentCaptor.forClass(
            String::class.java
        )
        Mockito.verify(editor).putString(keyCaptor.capture(), valueCaptor.capture())
        Assert.assertEquals(DeviceList.DEVICE_INFO_KEY, keyCaptor.value)
        Assert.assertEquals(TEST_DEVICE_GETTING_LIST_VALUES.toString(), valueCaptor.value)
    }

    companion object {
        private val TEST_DEVICE = DeviceInfo(
            "a", "b", "c", 1, -2, ConfidenceLevel.HIGH
        )
        private val TEST_DEVICE_GETTING_LIST_VALUES = DeviceInfo(
            "a",
            "b",
            "c",
            DeviceList.DEFAULT_INFO.rssiCorrection,
            DeviceList.DEFAULT_INFO.tx,
            ConfidenceLevel.NONE
        )

        private fun assertDevice(
            info: DeviceInfo,
            manu: String,
            device: String,
            model: String,
            rssi: Int,
            tx: Int,
            clevel: ConfidenceLevel
        ) {
            Assert.assertEquals(manu, info.manufacturer)
            Assert.assertEquals(device, info.device)
            Assert.assertEquals(model, info.model)
            Assert.assertEquals(rssi.toLong(), info.rssiCorrection.toLong())
            Assert.assertEquals(tx.toLong(), info.tx.toLong())
            Assert.assertEquals(clevel, info.calibrationConfidence)
        }
    }
}
