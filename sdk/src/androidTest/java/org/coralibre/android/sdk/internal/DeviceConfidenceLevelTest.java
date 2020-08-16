package org.coralibre.android.sdk.internal;

import org.coralibre.android.sdk.internal.device_info.ConfidenceLevel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeviceConfidenceLevelTest {
    @Test
    public void testGetConfidenceLevelFromString() {
        assertEquals(ConfidenceLevel.NONE, ConfidenceLevel.getConfidenceLevel("NONE"));
        assertEquals(ConfidenceLevel.LOW, ConfidenceLevel.getConfidenceLevel("LOW"));
        assertEquals(ConfidenceLevel.MEDIUM, ConfidenceLevel.getConfidenceLevel("MEDIUM"));
        assertEquals(ConfidenceLevel.HIGH, ConfidenceLevel.getConfidenceLevel("HIGH"));
    }

    @Test
    public void testGetConfidenceLevelFromInt() {
        assertEquals(ConfidenceLevel.NONE, ConfidenceLevel.getConfidenceLevel(0));
        assertEquals(ConfidenceLevel.LOW, ConfidenceLevel.getConfidenceLevel(1));
        assertEquals(ConfidenceLevel.MEDIUM, ConfidenceLevel.getConfidenceLevel(2));
        assertEquals(ConfidenceLevel.HIGH, ConfidenceLevel.getConfidenceLevel(3));
    }
}
