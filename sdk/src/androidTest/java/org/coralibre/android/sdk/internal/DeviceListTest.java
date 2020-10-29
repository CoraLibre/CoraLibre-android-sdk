package org.coralibre.android.sdk.internal;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel;
import org.coralibre.android.sdk.internal.deviceinfo.DeviceInfo;
import org.coralibre.android.sdk.internal.deviceinfo.DeviceList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.List;

import static org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel.HIGH;
import static org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel.LOW;
import static org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel.NONE;
import static org.coralibre.android.sdk.internal.deviceinfo.DeviceList.DEFAULT_INFO;
import static org.coralibre.android.sdk.internal.deviceinfo.DeviceList.DEVICE_INFO_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceListTest {
    private static final DeviceInfo TEST_DEVICE = new DeviceInfo(
            "a", "b", "c", 1, -2, HIGH);

    private static final DeviceInfo TEST_DEVICE_GETTING_LIST_VALUES = new DeviceInfo(
            "a", "b", "c", DEFAULT_INFO.getRssiCorrection(), DEFAULT_INFO.getTx(), NONE);

    private Context context;
    private DeviceList deviceList;

    private DeviceList.FindingsResult runFindingsInList(final DeviceList deviceList,
                                                        final String manufacturer,
                                                        final String device,
                                                        final String model) throws Exception {
        Method findingsInListMethod = DeviceList.class.getDeclaredMethod("findingsInList",
                String.class, String.class, String.class);
        findingsInListMethod.setAccessible(true);
        return (DeviceList.FindingsResult) findingsInListMethod.invoke(
                deviceList, manufacturer, device, model);
    }

    private DeviceInfo runGetAverageOfFindings(DeviceList deviceList, List<DeviceInfo> findings) throws Exception {
        Method getAverageOfFindingsMethod = DeviceList.class.getDeclaredMethod(
                "getAverageOfFindings", List.class);
        getAverageOfFindingsMethod.setAccessible(true);
        return (DeviceInfo) getAverageOfFindingsMethod.invoke(deviceList, findings);
    }

    private DeviceInfo runGetBestFittingInfoFormList(DeviceList deviceList,
                                                     String oem,
                                                     String device,
                                                     String model) throws Exception {
        Method getBestFittingInfoFromListMethod = DeviceList.class.getDeclaredMethod(
                "getBestFittingInfoFromList", String.class, String.class, String.class);
        getBestFittingInfoFromListMethod.setAccessible(true);
        return (DeviceInfo) getBestFittingInfoFromListMethod.invoke(deviceList, oem, device, model);
    }

    private DeviceInfo runGetOwnDeviceInfo(DeviceList deviceList,
                                           String oem,
                                           String device,
                                           String model) throws Exception {
        Method getOwnDeviceInfoMethod = DeviceList.class.getDeclaredMethod(
                "getOwnDeviceInfo", String.class, String.class, String.class);
        getOwnDeviceInfoMethod.setAccessible(true);
        return (DeviceInfo) getOwnDeviceInfoMethod.invoke(deviceList, oem, device, model);
    }


    @Before
    public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        SharedPreferences sharedPreferences = Mockito.mock(SharedPreferences.class);
        SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(any(), any())).thenReturn(editor);
        deviceList = new DeviceList(context, sharedPreferences);
    }


    private static void assertDevice(DeviceInfo info,
                                     String manu,
                                     String device,
                                     String model,
                                     int rssi,
                                     int tx,
                                     ConfidenceLevel clevel) {
        assertEquals(manu, info.getManufacturer());
        assertEquals(device, info.getDevice());
        assertEquals(model, info.getModel());
        assertEquals(rssi, info.getRssiCorrection());
        assertEquals(tx, info.getTx());
        assertEquals(clevel, info.getCalibrationConfidence());
    }

    @Test
    public void testLoadDeviceList() throws Exception {
        DeviceInfo[] deviceList = DeviceList.loadDeviceInfoTable(context);
        for (DeviceInfo i : deviceList) {
            System.out.println(i);
            assertTrue("manufacturer seems to be empty", i.getManufacturer().length() > 0);
            assertNotSame("confidence may not be NONE", i.getCalibrationConfidence(), NONE);
        }
    }

    @Test
    public void testLoadDeviceListLength() throws Exception {
        DeviceInfo[] deviceList = DeviceList.loadDeviceInfoTable(context);
        assertEquals(11809, deviceList.length);
    }

    @Test
    public void testGetInfoForKnownDevice() throws Exception {
        DeviceInfo info = deviceList.getExactInfo("asus", "P024_1", "P024");
        assertDevice(info, "asus", "P024_1", "P024", 3, -22, LOW);
    }

    @Test
    public void testGetInfoForUnknownDevice() throws Exception {
        DeviceInfo info = deviceList.getExactInfo("testphone", "versionone", "modelone");
        assertDevice(info, "testphone", "versionone", "modelone", 4, -25, NONE);
    }

    @Test
    public void testFindingsInListForUnknownSamsungZaninDevice() throws Exception {
        DeviceList.FindingsResult result =
                runFindingsInList(deviceList, "samsung", "zanin", "unknown");
        assertEquals(2340, result.manufacturerFindings.size());
        assertEquals(3, result.oemDeviceFindings.size());
        assertEquals(0, result.oemModelFindings.size());
    }

    @Test
    public void testFindingsInListForUnknownAsusA001dModel() throws Exception {
        DeviceList.FindingsResult result =
                runFindingsInList(deviceList, "asus", "unknown", "ASUS_A001D");
        assertEquals(347, result.manufacturerFindings.size());
        assertEquals(0, result.oemDeviceFindings.size());
        assertEquals(2, result.oemModelFindings.size());
    }

    @Test
    public void testGetAverageOfSamsungZaninDevice() throws Exception {
        DeviceList.FindingsResult findings =
            runFindingsInList(deviceList, "samsung", "zanin", "unknown");
        DeviceInfo oemAverage = DeviceList.getAverageOfFindings(findings.manufacturerFindings);
        DeviceInfo deviceAverage = DeviceList.getAverageOfFindings(findings.oemDeviceFindings);
        DeviceInfo modelAverage = DeviceList.getAverageOfFindings(findings.oemModelFindings);

        assertEquals(5, oemAverage.getRssiCorrection());
        assertEquals(-23, oemAverage.getTx());
        assertEquals(NONE, oemAverage.getCalibrationConfidence());

        assertEquals(5, deviceAverage.getRssiCorrection());
        assertEquals(-24, deviceAverage.getTx());
        assertEquals(NONE, deviceAverage.getCalibrationConfidence());

        assertEquals(0, modelAverage.getRssiCorrection());
        assertEquals(0, modelAverage.getTx());
        assertEquals(NONE, modelAverage.getCalibrationConfidence());
    }

    @Test
    public void testBestFittingForOneDeviceMatch() throws Exception {
        DeviceInfo info = runGetBestFittingInfoFormList(deviceList, "google", "blueline", "unknown");
        assertEquals(5, info.getRssiCorrection());
        assertEquals(-33, info.getTx());
        assertEquals(HIGH, info.getCalibrationConfidence());
    }

    @Test
    public void testBestFittingForOneModelMatch() throws Exception {
        DeviceInfo info = runGetBestFittingInfoFormList(deviceList, "google", "unknown", "Pixel 3");
        assertEquals(5, info.getRssiCorrection());
        assertEquals(-33, info.getTx());
        assertEquals(HIGH, info.getCalibrationConfidence());
    }

    @Test
    public void testBestFittingForKnownVendor() throws Exception {
        DeviceInfo info = runGetBestFittingInfoFormList(deviceList, "samsung", "unknown", "unknown");
        assertEquals(5, info.getRssiCorrection());
        assertEquals(-23, info.getTx());
        assertEquals(NONE, info.getCalibrationConfidence());
    }

    @Test
    public void testGetOwnInfoForKnownDevice() throws Exception {
        DeviceInfo info = runGetOwnDeviceInfo(deviceList, "google", "blueline", "Pixel 3");
        assertEquals(5, info.getRssiCorrection());
        assertEquals(-33, info.getTx());
        assertEquals(HIGH, info.getCalibrationConfidence());
    }

    @Test
    public void testGetOwnInfoForUnknownDevice() throws Exception {
        DeviceInfo info = runGetOwnDeviceInfo(deviceList, "unknown", "unknown", "unknown");
        assertEquals(4, info.getRssiCorrection());
        assertEquals(-25, info.getTx());
        assertEquals(NONE, info.getCalibrationConfidence());
    }

    @Test
    public void testGetOwnInfoFromSharedPreferences() throws Exception {
        final SharedPreferences prefs = Mockito.mock(SharedPreferences.class);
        when(prefs.contains(DEVICE_INFO_KEY)).thenReturn(true);
        when(prefs.getString(DEVICE_INFO_KEY, DEFAULT_INFO.toString()))
                .thenReturn(TEST_DEVICE.toString());

        DeviceList deviceList = new DeviceList(context, prefs);
        DeviceInfo info = runGetOwnDeviceInfo(deviceList,
                TEST_DEVICE.getManufacturer(),
                TEST_DEVICE.getDevice(),
                TEST_DEVICE.getModel());

        assertEquals(TEST_DEVICE.getManufacturer(), info.getManufacturer());
        assertEquals(TEST_DEVICE.getDevice(), info.getDevice());
        assertEquals(TEST_DEVICE.getModel(), info.getModel());
        assertEquals(TEST_DEVICE.getRssiCorrection(), info.getRssiCorrection());
        assertEquals(TEST_DEVICE.getTx(), info.getTx());
        assertEquals(TEST_DEVICE.getCalibrationConfidence(), info.getCalibrationConfidence());
    }

    @Test
    public void testGetOwnInfoFromListAndSaveItInPreferences() throws Exception {
        final SharedPreferences prefs = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor editor = Mockito.mock(SharedPreferences.Editor.class);
        when(prefs.contains(DEVICE_INFO_KEY)).thenReturn(false);
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(any(), any())).thenReturn(editor);

        DeviceList deviceList = new DeviceList(context, prefs);
        DeviceInfo info = runGetOwnDeviceInfo(deviceList,
                TEST_DEVICE.getManufacturer(),
                TEST_DEVICE.getDevice(),
                TEST_DEVICE.getModel());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(editor).putString(keyCaptor.capture(), valueCaptor.capture());
        assertEquals(DEVICE_INFO_KEY, keyCaptor.getValue());
        assertEquals(TEST_DEVICE_GETTING_LIST_VALUES.toString(), valueCaptor.getValue());
    }
}
