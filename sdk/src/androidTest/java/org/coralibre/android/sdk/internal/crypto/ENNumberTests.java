package org.coralibre.android.sdk.internal.crypto;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.crypto.ENNumber;
import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ENNumberTests {

    @Test
    public void testSetUnixTime() {
        ENNumber i = new ENNumber(10000000L, true);
        assertEquals(16666, i.get());
    }

    @Test
    public void testGetUnixTime() {
        ENNumber i = new ENNumber(1000L);
        assertEquals(600000, i.getUnixTime());
    }

    @Test
    public void testGetBytes5() {
        ENNumber i = new ENNumber(5L);
        byte[] data = i.getBytes();
        assertEquals(5, data[0]);
    }

    @Test
    public void testSetByte5() {
        byte[] val = new byte[4];
        val[0] = 5;
        ENNumber i = new ENNumber(val);
        assertEquals(5, i.get());
    }

    @Test
    public void testSet0x12345678() {
        byte[] val = new byte[] {0x78, 0x56, 0x34, 0x12};
        ENNumber i = new ENNumber(val);
        assertEquals(0x12345678, i.get());
    }

    @Test
    public void testGetBytes0x12345678() throws Exception {
        ENNumber i = new ENNumber(0x12345678);
        byte[] data = i.getBytes();
        assertArrayEquals(new byte[] {0x78, 0x56, 0x34, 0x12}, data);
    }
}
