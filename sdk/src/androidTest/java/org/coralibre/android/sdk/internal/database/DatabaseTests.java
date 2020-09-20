package org.coralibre.android.sdk.internal.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.coralibre.android.sdk.internal.crypto.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ENInterval;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DatabaseTests {


    @BeforeClass
    public static void initGlobal() {
        // Static, since otherwise JUnit throws an exception:
        // https://stackoverflow.com/questions/733037/why-isnt-my-beforeclass-method-running/733042#733042
        // Also see:
        // https://stackoverflow.com/questions/52873173/migrating-junit4-tests-to-androidx-what-causes-delegate-runner-could-not-be-lo

        DatabaseAccess.init(InstrumentationRegistry.getInstrumentation().getContext());
    }


    @AfterClass
    public static void deInit() {
        // If this call is not performed, tests from other classes where the database's init is
        // called might not run, since db reinitialization would throw an exception:
        DatabaseAccess.deInit();
    }


    @Test
    public void testInsertTek() {
        DatabaseAccess.getDefaultDatabaseInstance().clearAllData();

        // Insert:
        Random random = new Random();
        byte[] dumTekBytes = new byte[8];
        random.nextBytes(dumTekBytes);
        ENInterval dumIntervalNumber = new ENInterval(2000l);
        GeneratedTEK dumTek = new GeneratedTEK(dumIntervalNumber, dumTekBytes);
        DatabaseAccess.getDefaultDatabaseInstance().addGeneratedTEK(dumTek);

        // Query:
        Iterable<GeneratedTEK> resultTeks =
                DatabaseAccess.getDefaultDatabaseInstance().getAllGeneratedTEKs();

        GeneratedTEK resultTekForInterval =
                DatabaseAccess.getDefaultDatabaseInstance().getGeneratedTEK(dumIntervalNumber);

        // Compare:
        int numResultTeks = 0;
        for (GeneratedTEK resultTek : resultTeks) {
            numResultTeks ++;
            assertArrayEquals(dumTekBytes, resultTek.getKey());
            assertEquals(dumIntervalNumber, resultTek.getInterval());
        }
        assertEquals(1, numResultTeks);

        assertArrayEquals(dumTekBytes, resultTekForInterval.getKey());
        assertEquals(dumIntervalNumber, resultTekForInterval.getInterval());
    }


    @Test
    public void testInsertCapturedData() {
        DatabaseAccess.getDefaultDatabaseInstance().clearAllData();


        // Insert:
        Random random = new Random();
        byte[] dumPayload = new byte[8];
        random.nextBytes(dumPayload);
        byte[] dumRssiArray = new byte[1];
        random.nextBytes(dumRssiArray);
        byte dumRssi = dumRssiArray[0];
        Long dumCaptureTimestamp = 123545L;
        CapturedData dumData = new CapturedData(dumCaptureTimestamp, dumRssi, dumPayload);
        DatabaseAccess.getDefaultDatabaseInstance().addCapturedPayload(dumData);

        // Query:
        Iterable<IntervalOfCapturedData> resultIntervals =
                DatabaseAccess.getDefaultDatabaseInstance().getAllCollectedPayload();

        // Compare:
        int numResultIntervals = 0;
        int numResultData = 0;
        for (IntervalOfCapturedData resultInterval : resultIntervals) {
            numResultIntervals ++;


            for (CapturedData resultData : resultInterval.getCapturedData()) {
                numResultData ++;
                assertArrayEquals(dumPayload, resultData.getPayload());
                assertEquals(dumRssi, resultData.getRssi());
                assertEquals(dumCaptureTimestamp, resultData.getCaptureTimestamp());
            }
        }
        assertEquals(1, numResultIntervals);
        assertEquals(1, numResultData);
    }



    @Test
    public void testTruncate() {
        DatabaseAccess.getDefaultDatabaseInstance().clearAllData();

        ENInterval now = CryptoModule.getCurrentInterval();
        ENInterval intervalRemove = new ENInterval(now.get() - 24 * 6 * 14 - 1);
        ENInterval intervalKeep = new ENInterval(now.get() - 24 * 6 * 14);


        // Insert:
        Random random = new Random();

        {
            byte[] tekRemoveBytes = new byte[8];
            random.nextBytes(tekRemoveBytes);
            GeneratedTEK tekRemove = new GeneratedTEK(intervalRemove, tekRemoveBytes);
            DatabaseAccess.getDefaultDatabaseInstance().addGeneratedTEK(tekRemove);
        }

        byte[] tekKeepBytes = new byte[8];
        random.nextBytes(tekKeepBytes);
        GeneratedTEK tekKeep = new GeneratedTEK(intervalKeep, tekKeepBytes);
        DatabaseAccess.getDefaultDatabaseInstance().addGeneratedTEK(tekKeep);

        {
            byte[] payloadRemove = new byte[8];
            random.nextBytes(payloadRemove);
            byte[] rssiRemoveArray = new byte[1];
            random.nextBytes(rssiRemoveArray);
            byte rssiRemove = rssiRemoveArray[0];
            long timestampRemove = intervalRemove.getUnixTime();
            CapturedData dataRemove = new CapturedData(timestampRemove, rssiRemove, payloadRemove);
            DatabaseAccess.getDefaultDatabaseInstance().addCapturedPayload(dataRemove);
        }

        byte[] payloadKeep = new byte[8];
        random.nextBytes(payloadKeep);
        byte[] rssiKeepArray = new byte[1];
        random.nextBytes(rssiKeepArray);
        byte rssiKeep = rssiKeepArray[0];
        Long timestampKeep = intervalKeep.getUnixTime();
        CapturedData dataKeep = new CapturedData(timestampKeep, rssiKeep, payloadKeep);
        DatabaseAccess.getDefaultDatabaseInstance().addCapturedPayload(dataKeep);


        // Truncate:
        DatabaseAccess.getDefaultDatabaseInstance().truncateLast14Days();

        // Query:
        Iterable<GeneratedTEK> resultTeks =
                DatabaseAccess.getDefaultDatabaseInstance().getAllGeneratedTEKs();
        Iterable<IntervalOfCapturedData> resultIntervals =
                DatabaseAccess.getDefaultDatabaseInstance().getAllCollectedPayload();

        // Compare:
        int numResultTeks = 0;
        for (GeneratedTEK resultTek : resultTeks) {
            numResultTeks ++;
            assertArrayEquals(tekKeepBytes, resultTek.getKey());
            assertEquals(intervalKeep, resultTek.getInterval());
        }
        assertEquals(1, numResultTeks);


        int numResultIntervals = 0;
        int numResultData = 0;
        for (IntervalOfCapturedData resultInterval : resultIntervals) {
            numResultIntervals ++;

            for (CapturedData resultData : resultInterval.getCapturedData()) {
                numResultData ++;
                assertArrayEquals(payloadKeep, resultData.getPayload());
                assertEquals(rssiKeep, resultData.getRssi());
                assertEquals(timestampKeep, resultData.getCaptureTimestamp());
            }
        }
        assertEquals(1, numResultIntervals);
        assertEquals(1, numResultData);

    }



}
