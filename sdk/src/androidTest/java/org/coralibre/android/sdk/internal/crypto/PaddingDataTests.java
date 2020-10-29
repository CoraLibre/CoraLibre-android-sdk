package org.coralibre.android.sdk.internal.crypto;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PaddingDataTests {
    private static final ENInterval TIMESTAMP_1 = new ENInterval(0x12345678L);
    private static final byte[] PADDED_DATA_1 = new byte[] {
            69, 78, 45, 82, 80, 73,
            0, 0, 0, 0, 0, 0,
            0x78, 0x56, 0x34, 0x12
    };

    private static final byte[] PADDED_DATA_BROKEN = new byte[] {
            69, 67, 45, 34, 80, 73,
            0, 0, 0, 0, 0, 0,
            0x78, 0x56, 0x34, 0x12
    };

    @Test
    public void testSetInterval() {
        PaddedData pd = new PaddedData(TIMESTAMP_1);
        assertArrayEquals(PADDED_DATA_1, pd.getData());
    }

    @Test
    public void testSetRawPaddedData() {
        PaddedData pd = new PaddedData(PADDED_DATA_1);
        assertEquals(TIMESTAMP_1.get(), pd.getInterval().get());
    }

    @Test
    public void testIsValidTrue() {
        PaddedData pd = new PaddedData(PADDED_DATA_1);
        assertTrue(pd.isRPIInfoValid());
    }

    @Test
    public void testIsValidFalse() {
        PaddedData pd = new PaddedData(PADDED_DATA_BROKEN);
        assertFalse(pd.isRPIInfoValid());
    }
}
