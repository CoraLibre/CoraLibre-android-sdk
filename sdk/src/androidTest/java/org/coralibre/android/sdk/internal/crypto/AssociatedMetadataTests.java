package org.coralibre.android.sdk.internal.crypto;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.crypto.AssociatedMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AssociatedMetadataTests {
    private static final byte[] AM_VAL_V1_0db = {0b01000000, 0x00, 0x00, 0x00};
    private static final byte[] AM_VAL_V2_1_10db = {(byte) 0b10010000, 0x0A, 0x00, 0x00};
    private static final byte[] AM_VAL_V3_2_MINUS16db = {(byte) 0b11100000, (byte) 0xF0, 0x00, 0x00};

    @Test
    public void testSetValV1() {
        AssociatedMetadata am = new AssociatedMetadata(1, 0, 0);
        assertArrayEquals(AM_VAL_V1_0db, am.getData());
    }

    @Test
    public void testSetValV2() {
        AssociatedMetadata am = new AssociatedMetadata(2, 1, 10);
        assertArrayEquals(AM_VAL_V2_1_10db, am.getData());
    }

    @Test
    public void testSetValV3() {
        AssociatedMetadata am = new AssociatedMetadata(3, 2, -16);
        assertArrayEquals(AM_VAL_V3_2_MINUS16db, am.getData());
    }

    @Test
    public void testGetMajorVersionV1() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V1_0db);
        assertEquals(1, am.getMajorVersion());
    }

    @Test
    public void testGetMajorVersionV2() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V2_1_10db);
        assertEquals(2, am.getMajorVersion());
    }

    @Test
    public void testGetMajorVersionV3() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V3_2_MINUS16db);
        assertEquals(3, am.getMajorVersion());
    }

    @Test
    public void testGetMinorVersionV1() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V1_0db);
        assertEquals(0, am.getMinorVersion());
    }

    @Test
    public void testGetMinorVersionV2() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V2_1_10db);
        assertEquals(1, am.getMinorVersion());
    }

    @Test
    public void testGetMinorVersionV3() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V3_2_MINUS16db);
        assertEquals(2, am.getMinorVersion());
    }

    @Test
    public void testGetPowerLevelV1() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V1_0db);
        assertEquals(0, am.getTransmitPowerLevel());
    }

    @Test
    public void testGetPowerLevelV2() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V2_1_10db);
        assertEquals(10, am.getTransmitPowerLevel());
    }

    @Test
    public void testGetPowerLevelV3() {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V3_2_MINUS16db);
        assertEquals(-16, am.getTransmitPowerLevel());
    }
}
