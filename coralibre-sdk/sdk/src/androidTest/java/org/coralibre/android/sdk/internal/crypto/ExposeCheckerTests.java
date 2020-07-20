package org.coralibre.android.sdk.internal.crypto;

import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.ExposeChecker;
import org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier.RPI_SIZE;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.TEK_LENGTH;
import static org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey.TEK_ROLLING_PERIOD;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExposeCheckerTests {
    private static byte[] hex2byte(String hex) {
        return new BigInteger(hex,16).toByteArray();
    }

    private static TemporaryExposureKey tek(long whichRollingPeriod, String hex) {
        assertEquals(2*TEK_LENGTH, hex.length());
        return new TemporaryExposureKey(new ENNumber(whichRollingPeriod * TEK_ROLLING_PERIOD),
                hex2byte(hex));
    }

    private static RollingProximityIdentifier rollingProximityIdentifier(long rawENNumber, String hex) {
        assertEquals(2*RPI_SIZE, hex.length());
        return new RollingProximityIdentifier(hex2byte(hex), new ENNumber(rawENNumber));
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static final List<TemporaryExposureKey> TEK_LIST =
            new ArrayList<>(Arrays.asList(
                    tek(10, "11111111111111111111111111111111"),
                    tek(11, "22222222222222222222222222222222"),
                    tek(12, "33333333333333333333333333333333"),
                    tek(13, "44444444444444444444444444444444"),
                    tek(14, "55555555555555555555555555555555"),
                    tek(15, "66666666666666666666666666666666"),
                    tek(16, "77777777777777777777777777777777"),
                    tek(17, "88888888888888888888888888888888"),
                    tek(18, "99999999999999999999999999999999"),
                    tek(19, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")));

    private static final List<TemporaryExposureKey> TEK_LIST_TWO_PER_DAY =
            new ArrayList<>(Arrays.asList(
                    tek(10, "11111111111111111111111111111111"),
                    tek(10, "10101010101010101010101010101010"),
                    tek(11, "22222222222222222222222222222222"),
                    tek(11, "20202020202020202020202020202020"),
                    tek(12, "33333333333333333333333333333333"),
                    tek(12, "30303030303030303030303030303030"),
                    tek(13, "44444444444444444444444444444444"),
                    tek(13, "40404040404040404040404040404040"),
                    tek(14, "55555555555555555555555555555555"),
                    tek(14, "50505050505050505050505050505050"),
                    tek(15, "66666666666666666666666666666666"),
                    tek(15, "60606060606060606060606060606060"),
                    tek(16, "77777777777777777777777777777777"),
                    tek(16, "70707070707070707070707070707070"),
                    tek(17, "88888888888888888888888888888888"),
                    tek(17, "80808080808080808080808080808080"),
                    tek(18, "99999999999999999999999999999999"),
                    tek(18, "90909090909090909090909090909090"),
                    tek(19, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                    tek(19, "a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0a0")));

    private static final ENNumber MIDDLE_OF_DAY_14 =
            new ENNumber((long)(13.5 * TEK_ROLLING_PERIOD));
    private static final ENNumber ONE_HOUR_INTO_DAY_15 =
            new ENNumber((long)(14 * TEK_ROLLING_PERIOD + 6));

    private static final long SLOTSTART_MIDDLE_OF_DAY_14 = (long)(13.5 * TEK_ROLLING_PERIOD - 12);
    private static final long SLOTEND_MIDDLE_OF_DAY_14 = (long)(13.5 * TEK_ROLLING_PERIOD + 12);

    private static final long SLOTSTART_ONE_HOUR_INTO_DAY_15 = 14 * TEK_ROLLING_PERIOD;
    private static final long SLOTEND_ONE_HOUR_INTO_DAY_15 = 14 * TEK_ROLLING_PERIOD + 6 + 12;
    private static final long SLOTSTART_END_DAY_14 = 14 * TEK_ROLLING_PERIOD + 6 - 12;
    private static final long SLOTEND_END_DAY_14 = SLOTSTART_ONE_HOUR_INTO_DAY_15;

    @Test
    public void testGetAllRelatedTEKsDuringDay() {
        List<TemporaryExposureKey> tekSubSet =
                ExposeChecker.getAllRelatedTEKs(TEK_LIST, MIDDLE_OF_DAY_14);
        assertEquals(1, tekSubSet.size());
        assertArrayEquals(TEK_LIST.get(3).getKey(), tekSubSet.get(0).getKey());
    }

    @Test
    public void testGetAllRelatedTEKsAtBeginningOfDayTwoPerDay() {
        List<TemporaryExposureKey> tekSubSet =
                ExposeChecker.getAllRelatedTEKs(TEK_LIST_TWO_PER_DAY, ONE_HOUR_INTO_DAY_15);
        assertEquals(4, tekSubSet.size());
        assertArrayEquals(TEK_LIST_TWO_PER_DAY.get(6).getKey(), tekSubSet.get(0).getKey());
        assertArrayEquals(TEK_LIST_TWO_PER_DAY.get(7).getKey(), tekSubSet.get(1).getKey());
        assertArrayEquals(TEK_LIST_TWO_PER_DAY.get(8).getKey(), tekSubSet.get(2).getKey());
        assertArrayEquals(TEK_LIST_TWO_PER_DAY.get(9).getKey(), tekSubSet.get(3).getKey());
    }

    @Test
    public void testGetAllRelatedTEKsAtBeginningOfDay() {
        List<TemporaryExposureKey> tekSubSet =
                ExposeChecker.getAllRelatedTEKs(TEK_LIST, ONE_HOUR_INTO_DAY_15);
        assertEquals(2, tekSubSet.size());
        assertArrayEquals(TEK_LIST.get(3).getKey(), tekSubSet.get(0).getKey());
        assertArrayEquals(TEK_LIST.get(4).getKey(), tekSubSet.get(1).getKey());
    }

    @Test
    public void testGenerateRPIsForSlotDuringDay() {
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST.get(3), MIDDLE_OF_DAY_14);
        assertEquals(25, genrpis.size());

        int i = 0;
        for(long interv = SLOTSTART_MIDDLE_OF_DAY_14;
            interv <= SLOTEND_MIDDLE_OF_DAY_14;
            interv++) {
            assertEquals("For RPI: " + i + " the interval does not fit.",
                    interv,
                    genrpis.get(i).getInterval().get());
            i++;
        }
    }

    @Test
    public void testGenerateRPIsForSlotAtBeginningOfDay() {
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST.get(4), ONE_HOUR_INTO_DAY_15);
        assertEquals(19, genrpis.size());

        int i = 0;
        for(long interv = SLOTSTART_ONE_HOUR_INTO_DAY_15;
            interv <= SLOTEND_ONE_HOUR_INTO_DAY_15;
            interv++) {
            assertEquals("For RPI: " + i + " the interval does not fit.",
                    interv,
                    genrpis.get(i).getInterval().get());
            i++;
        }
    }

    @Test
    public void testGenerateRPIsForSlotAtEndingOfDay() {
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST.get(3), ONE_HOUR_INTO_DAY_15);
        assertEquals(7, genrpis.size());

        int i = 0;
        for(long interv = SLOTSTART_END_DAY_14;
            interv <= SLOTEND_END_DAY_14;
            interv++) {
            assertEquals("For RPI: " + i + " the interval does not fit.",
                    interv,
                    genrpis.get(i).getInterval().get());
            i++;
        }
    }

    @Test
    public void testFindMatches() {
        List<TemporaryExposureKey> tekSubSet =
                ExposeChecker.getAllRelatedTEKs(TEK_LIST_TWO_PER_DAY, MIDDLE_OF_DAY_14);
        List<RollingProximityIdentifier> genrpis =
                ExposeChecker.generateRPIsForSlot(TEK_LIST_TWO_PER_DAY.get(6), MIDDLE_OF_DAY_14);
        genrpis.addAll(
                ExposeChecker.generateRPIsForSlot(TEK_LIST_TWO_PER_DAY.get(7), MIDDLE_OF_DAY_14));

        List<RollingProximityIdentifier> collectedRPIs =
                new ArrayList<>(Arrays.asList(
                        genrpis.get(5),
                        genrpis.get(27)));

        List<Pair<TemporaryExposureKey, RollingProximityIdentifier>> foundMatches
                = ExposeChecker.findMatches(TEK_LIST_TWO_PER_DAY, collectedRPIs);

        assertEquals(2, foundMatches.size());
        assertArrayEquals(foundMatches.get(0).first.getKey(), TEK_LIST_TWO_PER_DAY.get(6).getKey());
        assertEquals(foundMatches.get(0).second, genrpis.get(5));
        assertArrayEquals(foundMatches.get(1).first.getKey(), TEK_LIST_TWO_PER_DAY.get(7).getKey());
        assertEquals(foundMatches.get(1).second, genrpis.get(27));
    }
}
