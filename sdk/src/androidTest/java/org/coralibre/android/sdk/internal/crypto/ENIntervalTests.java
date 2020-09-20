package org.coralibre.android.sdk.internal.crypto;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ENIntervalTests {

    @Test
    public void testSetUnixTime() {
        ENInterval i = new ENInterval(10000000L, true);
        assertEquals(16666, i.get());
    }

    @Test
    public void testGetUnixTime() {
        ENInterval i = new ENInterval(1000L);
        assertEquals(600000, i.getUnixTime());
    }

    @Test
    public void testGetBytes5() {
        ENInterval i = new ENInterval(5L);
        byte[] data = i.getBytes();
        assertEquals(5, data[0]);
    }

    @Test
    public void testSetByte5() {
        byte[] val = new byte[4];
        val[0] = 5;
        ENInterval i = new ENInterval(val);
        assertEquals(5, i.get());
    }

    @Test
    public void testSet0x12345678() {
        byte[] val = new byte[] {0x78, 0x56, 0x34, 0x12};
        ENInterval i = new ENInterval(val);
        assertEquals(0x12345678, i.get());
    }

    @Test
    public void testGetBytes0x12345678() throws Exception {
        ENInterval i = new ENInterval(0x12345678);
        byte[] data = i.getBytes();
        assertArrayEquals(new byte[] {0x78, 0x56, 0x34, 0x12}, data);
    }
}
