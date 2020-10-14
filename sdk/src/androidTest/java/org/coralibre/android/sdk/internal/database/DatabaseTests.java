package org.coralibre.android.sdk.internal.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.coralibre.android.sdk.DatatypesTestUtil;
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.datatypes.TemporaryExposureKey_internal;
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;
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
        byte[] dumTekBytes = new byte[16];
        random.nextBytes(dumTekBytes);
        ENInterval dumInterval =
            new ENInterval(ENIntervalUtil.getMidnight(2000l));
        TemporaryExposureKey_internal dumTek = new TemporaryExposureKey_internal(dumInterval, dumTekBytes);
        DatabaseAccess.getDefaultDatabaseInstance().addGeneratedTEK(dumTek);

        // Query:
        Iterable<TemporaryExposureKey_internal> resultTeks =
                DatabaseAccess.getDefaultDatabaseInstance().getAllOwnTEKs();

        TemporaryExposureKey_internal resultTekForInterval =
                DatabaseAccess.getDefaultDatabaseInstance().getOwnTEK(dumInterval);

        // Compare:
        int numResultTeks = 0;
        for (TemporaryExposureKey_internal resultTek : resultTeks) {
            numResultTeks ++;
            assertArrayEquals(dumTekBytes, resultTek.getKey());
            assertEquals(dumInterval, resultTek.getInterval());
        }
        assertEquals(1, numResultTeks);

        assertArrayEquals(dumTekBytes, resultTekForInterval.getKey());
        assertEquals(dumInterval, resultTekForInterval.getInterval());
    }


    @Test
    public void testInsertCapturedData() throws Exception {
        DatabaseAccess.getDefaultDatabaseInstance().clearAllData();


        // Insert:
        Random random = new Random();
        byte[] dumRpi = new byte[16];
        random.nextBytes(dumRpi);
        byte[] dumAem = new byte[4];
        random.nextBytes(dumAem);
        byte[] dumRssiArray = new byte[1];
        random.nextBytes(dumRssiArray);
        byte dumRssi = dumRssiArray[0];
        Long dumCaptureTimestamp = 123545L;
        CapturedData dumData = new CapturedData(
            dumCaptureTimestamp,
            dumRssi,
            new RollingProximityIdentifier(
                dumRpi,
                ENIntervalUtil.createFromUnixTimestamp(dumCaptureTimestamp)
            ),
            new AssociatedEncryptedMetadata(dumAem)
        );
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
                assertArrayEquals(dumRpi, resultData.getRpi().getData());
                assertArrayEquals(dumAem, resultData.getAem().getData());
                assertEquals(dumRssi, resultData.getRssi());
                assertEquals(dumCaptureTimestamp, resultData.getCaptureTimestamp());
            }
        }
        assertEquals(1, numResultIntervals);
        assertEquals(1, numResultData);
    }



    @Test
    public void testTruncate() throws Exception {
        DatabaseAccess.getDefaultDatabaseInstance().clearAllData();

        ENInterval now = ENIntervalUtil.getCurrentInterval();
        ENInterval intervalRemove = ENIntervalUtil.getMidnight(
            new ENInterval(now.get() - 24 * 6 * 14)
        );
        ENInterval intervalKeep = ENIntervalUtil.getMidnight(
            new ENInterval(now.get() - 24 * 6 * 13)
        );


        // Insert:
        Random random = new Random();

        {
            byte[] tekRemoveBytes = new byte[16];
            random.nextBytes(tekRemoveBytes);
            TemporaryExposureKey_internal tekRemove = new TemporaryExposureKey_internal(intervalRemove, tekRemoveBytes);
            DatabaseAccess.getDefaultDatabaseInstance().addGeneratedTEK(tekRemove);
        }

        byte[] tekKeepBytes = new byte[16];
        random.nextBytes(tekKeepBytes);
        TemporaryExposureKey_internal tekKeep = new TemporaryExposureKey_internal(intervalKeep, tekKeepBytes);
        DatabaseAccess.getDefaultDatabaseInstance().addGeneratedTEK(tekKeep);

        {
            byte[] rpiRemove = new byte[16];
            random.nextBytes(rpiRemove);
            byte[] aemRemove = new byte[4];
            random.nextBytes(aemRemove);
            byte[] rssiRemoveArray = new byte[1];
            random.nextBytes(rssiRemoveArray);
            byte rssiRemove = rssiRemoveArray[0];
            long timestampRemove = intervalRemove.getUnixTime();
            CapturedData dataRemove = new CapturedData(
                timestampRemove,
                rssiRemove,
                new RollingProximityIdentifier(rpiRemove, ENIntervalUtil.createFromUnixTimestamp(timestampRemove)),
                new AssociatedEncryptedMetadata(aemRemove)
            );
            DatabaseAccess.getDefaultDatabaseInstance().addCapturedPayload(dataRemove);
        }

        byte[] rpiKeep = new byte[16];
        random.nextBytes(rpiKeep);
        byte[] aemKeep = new byte[4];
        random.nextBytes(aemKeep);
        byte[] rssiKeepArray = new byte[1];
        random.nextBytes(rssiKeepArray);
        byte rssiKeep = rssiKeepArray[0];
        Long timestampKeep = intervalKeep.getUnixTime();
        CapturedData dataKeep = new CapturedData(
            timestampKeep,
            rssiKeep,
            new RollingProximityIdentifier(rpiKeep, intervalKeep),
            new AssociatedEncryptedMetadata(aemKeep)
        );
        DatabaseAccess.getDefaultDatabaseInstance().addCapturedPayload(dataKeep);


        // Truncate:
        DatabaseAccess.getDefaultDatabaseInstance().truncateLast14Days();

        // Query:
        Iterable<TemporaryExposureKey_internal> resultTeks =
                DatabaseAccess.getDefaultDatabaseInstance().getAllOwnTEKs();
        Iterable<IntervalOfCapturedData> resultIntervals =
                DatabaseAccess.getDefaultDatabaseInstance().getAllCollectedPayload();

        // Compare:
        int numResultTeks = 0;
        for (TemporaryExposureKey_internal resultTek : resultTeks) {
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
                assertArrayEquals(rpiKeep, resultData.getRpi().getData());
                assertArrayEquals(aemKeep, resultData.getAem().getData());
                assertEquals(rssiKeep, resultData.getRssi());
                assertEquals(timestampKeep, resultData.getCaptureTimestamp());
            }
        }
        assertEquals(1, numResultIntervals);
        assertEquals(1, numResultData);

    }



    @Test
    public void testAddDiagnosisKeys() {
        Database db = DatabaseAccess.getDefaultDatabaseInstance();
        db.clearAllData();

        LinkedList<DiagnosisKey> diagKeys = new LinkedList<DiagnosisKey>();

        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey());
        String token0 = "token0";
        db.addDiagnosisKeys(token0, diagKeys);

        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey());
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey());
        String token1 = "token1";
        db.addDiagnosisKeys(token1, diagKeys);


        // First verify that insertion with different tokens works:
        {
            List<DiagnosisKey> result0 = db.getDiagnosisKeys(token0);
            assertEquals(1, result0.size());

            List<DiagnosisKey> result1 = db.getDiagnosisKeys(token1);
            assertEquals(3, result1.size());
        }

        // Now add additional keys for an existing token:
        db.addDiagnosisKeys(token1, diagKeys);
        {
            List<DiagnosisKey> result0 = db.getDiagnosisKeys(token0);
            assertEquals(1, result0.size());

            List<DiagnosisKey> result1 = db.getDiagnosisKeys(token1);
            assertEquals(6, result1.size());
        }
    }

    @Test
    public void testDeleteToken() {
        Database db = DatabaseAccess.getDefaultDatabaseInstance();
        db.clearAllData();

        LinkedList<DiagnosisKey> diagKeys = new LinkedList<DiagnosisKey>();

        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey());
        String token0 = "token0";
        db.addDiagnosisKeys(token0, diagKeys);

        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey());
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey());
        String token1 = "token1";
        db.addDiagnosisKeys(token1, diagKeys);

        // Verify insertion worked as expected:
        {
            List<DiagnosisKey> result0 = db.getDiagnosisKeys(token0);
            assertEquals(1, result0.size());

            List<DiagnosisKey> result1 = db.getDiagnosisKeys(token1);
            assertEquals(3, result1.size());
        }

        // Now delete token1 and check again:
        db.deleteTokenWithDiagnosisKeys(token1);
        {
            List<DiagnosisKey> result0 = db.getDiagnosisKeys(token0);
            assertEquals(1, result0.size());

            List<DiagnosisKey> result1 = db.getDiagnosisKeys(token1);
            assertEquals(0, result1.size());
        }
    }

}
