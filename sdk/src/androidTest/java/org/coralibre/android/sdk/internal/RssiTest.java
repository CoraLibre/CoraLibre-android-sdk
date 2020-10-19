package org.coralibre.android.sdk.internal;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RssiTest {

    // I wasn't sure, how the casting works, so I wrote this test.

    @Test
    public void testPositiveRssiCasting() {
        int i_rssi = 127;
        byte b_rssi = new Integer(i_rssi).byteValue();
        assertEquals(i_rssi, new Byte(b_rssi).intValue());
    }

    @Test
    public void testNegativeRssiCasting() {
        int i_rssi = -127;
        byte b_rssi = new Integer(i_rssi).byteValue();
        assertEquals(i_rssi, new Byte(b_rssi).intValue());
    }


}
