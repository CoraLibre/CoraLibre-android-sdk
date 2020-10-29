package org.coralibre.android.sdk.internal;

import org.coralibre.android.sdk.internal.deviceinfo.ConfidenceLevel;
import org.coralibre.android.sdk.internal.deviceinfo.DeviceInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceInfoTest {
    @Test
    public void testCompareForDifferentManufacturers() {
        DeviceInfo device1 = new DeviceInfo("asus", "P024_1", "P024", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("blu", "C070", "C4 2019", 1, 1, ConfidenceLevel.LOW);
        assertTrue("device 1 earlier that device 2", 0 > device1.compareTo(device2));
        assertTrue("device 1 later that device 2", 0 < device2.compareTo(device1));
        assertEquals("device equals to itself", device1, device1);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device1));
    }

    @Test
    public void testCompareForDifferentManufacturersCaseInsensitive() {
        DeviceInfo device1 = new DeviceInfo("asus", "P024_1", "P024", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("BLU", "C070", "C4 2019", 1, 1, ConfidenceLevel.LOW);
        assertTrue("device 1 earlier that device 2", 0 > device1.compareTo(device2));
        assertTrue("device 1 later that device 2", 0 < device2.compareTo(device1));
        assertEquals("device equals to itself", device1, device1);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device1));
    }

    @Test
    public void testCompareForDifferentModels() {
        DeviceInfo device1 = new DeviceInfo("asus", "K019_2", "K019", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("asus", "P024_2", "P024", 1, 1, ConfidenceLevel.LOW);
        assertTrue("device 1 earlier that device 2", 0 > device1.compareTo(device2));
        assertTrue("device 1 later that device 2", 0 < device2.compareTo(device1));
        assertEquals("device equals to itself", device1, device1);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device1));
    }

    @Test
    public void testCompareForDifferentModelsCaseInsensitive() {
        DeviceInfo device1 = new DeviceInfo("asus", "K019_2", "K019", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("asus", "P024_2", "p024", 1, 1, ConfidenceLevel.LOW);
        assertTrue("device 1 earlier that device 2", 0 > device1.compareTo(device2));
        assertTrue("device 1 later that device 2", 0 < device2.compareTo(device1));
        assertEquals("device equals to itself", device1, device1);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device1));
    }

    @Test
    public void testCompareForDifferentDevices() {
        DeviceInfo device1 = new DeviceInfo("asus", "P024_1", "P024", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("asus", "P024_2", "P024", 1, 1, ConfidenceLevel.LOW);
        assertTrue("device 1 earlier that device 2", 0 > device1.compareTo(device2));
        assertTrue("device 1 later that device 2", 0 < device2.compareTo(device1));
        assertEquals("device equals to itself", device1, device1);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device1));
    }

    @Test
    public void testCompareForDifferentDevicesCaseInsensitive() {
        DeviceInfo device1 = new DeviceInfo("asus", "P024_1", "P024", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("asus", "p024_2", "P024", 1, 1, ConfidenceLevel.LOW);
        assertTrue("device 1 earlier that device 2", 0 > device1.compareTo(device2));
        assertTrue("device 1 later that device 2", 0 < device2.compareTo(device1));
        assertEquals("device equals to itself", device1, device1);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device1));
    }

    @Test
    public void testCaseInsensitivityForEquals() {
        DeviceInfo device1 = new DeviceInfo("AsUs", "p024_1", "P024", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("asuS", "P024_1", "p024", 1, 1, ConfidenceLevel.LOW);
        assertEquals("device equals to itself", device1, device2);
    }

    @Test
    public void testCaseInsensitivityForComparison() {
        DeviceInfo device1 = new DeviceInfo("AsUs", "p024_1", "P024", 1, 1, ConfidenceLevel.LOW);
        DeviceInfo device2 = new DeviceInfo("asuS", "P024_1", "p024", 1, 1, ConfidenceLevel.LOW);
        assertEquals("device equals to itself", device1, device2);
        assertEquals("device equals to itself by comparison", 0, device1.compareTo(device2));
    }
}
