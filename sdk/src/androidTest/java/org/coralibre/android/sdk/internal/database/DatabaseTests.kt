package org.coralibre.android.sdk.internal.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.coralibre.android.sdk.DatatypesTestUtil
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata
import org.coralibre.android.sdk.internal.datatypes.CapturedData
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.createFromUnixTimestamp
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.currentInterval
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.getMidnight
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.LinkedList
import java.util.Random

@RunWith(AndroidJUnit4::class)
class DatabaseTests {
    private lateinit var database: Database

    @Before
    fun initializeDatabase() {
        database = PersistentDatabase(InstrumentationRegistry.getInstrumentation().context, true)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun testInsertTek() {
        // Insert:
        val random = Random()
        val dumTekBytes = ByteArray(16)
        random.nextBytes(dumTekBytes)
        val dumInterval = ENInterval(getMidnight(2000L))
        val dumTek = InternalTemporaryExposureKey(dumInterval, dumTekBytes)
        database.addGeneratedTEK(dumTek)

        // Query:
        val resultTeks = database.getAllOwnTEKs()
        val resultTekForInterval = database.getOwnTEK(dumInterval)

        // Compare:
        var numResultTeks = 0
        for (resultTek in resultTeks) {
            numResultTeks++
            assertArrayEquals(dumTekBytes, resultTek.key)
            assertEquals(dumInterval, resultTek.interval)
        }
        assertEquals(1, numResultTeks.toLong())
        assertArrayEquals(dumTekBytes, resultTekForInterval.key)
        assertEquals(dumInterval, resultTekForInterval.interval)
    }

    @Test
    @Throws(Exception::class)
    fun testInsertCapturedData() {
        // Insert:
        val random = Random()
        val dumRpi = ByteArray(16)
        random.nextBytes(dumRpi)
        val dumAem = ByteArray(4)
        random.nextBytes(dumAem)
        val dumRssiArray = ByteArray(1)
        random.nextBytes(dumRssiArray)
        val dumRssi = dumRssiArray[0]
        val dumCaptureTimestamp = 123545L
        val dumData = CapturedData(
            dumCaptureTimestamp,
            dumRssi,
            RollingProximityIdentifier(
                dumRpi,
                createFromUnixTimestamp(dumCaptureTimestamp)
            ),
            AssociatedEncryptedMetadata(dumAem)
        )
        database.addCapturedPayload(dumData)

        // Query:
        val resultIntervals = database.getAllCollectedPayload()

        // Compare:
        var numResultIntervals = 0
        var numResultData = 0
        for (resultInterval in resultIntervals) {
            numResultIntervals++
            for (resultData in resultInterval.getCapturedData()) {
                numResultData++
                assertArrayEquals(dumRpi, resultData.rpi.getData())
                assertArrayEquals(dumAem, resultData.aem.data)
                assertEquals(dumRssi.toLong(), resultData.rssi.toLong())
                assertEquals(dumCaptureTimestamp, resultData.captureTimestampMillis)
            }
        }
        assertEquals(1, numResultIntervals.toLong())
        assertEquals(1, numResultData.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testTruncate() {
        val now = currentInterval
        val intervalRemove = getMidnight(
            ENInterval(now.get() - 24 * 6 * 14)
        )
        val intervalKeep = getMidnight(
            ENInterval(now.get() - 24 * 6 * 13)
        )

        // Insert:
        val random = Random()
        run {
            val tekRemoveBytes = ByteArray(16)
            random.nextBytes(tekRemoveBytes)
            val tekRemove = InternalTemporaryExposureKey(intervalRemove, tekRemoveBytes)
            database.addGeneratedTEK(tekRemove)
        }
        val tekKeepBytes = ByteArray(16)
        random.nextBytes(tekKeepBytes)
        val tekKeep = InternalTemporaryExposureKey(intervalKeep, tekKeepBytes)
        database.addGeneratedTEK(tekKeep)
        run {
            val rpiRemove = ByteArray(16)
            random.nextBytes(rpiRemove)
            val aemRemove = ByteArray(4)
            random.nextBytes(aemRemove)
            val rssiRemoveArray = ByteArray(1)
            random.nextBytes(rssiRemoveArray)
            val rssiRemove = rssiRemoveArray[0]
            val timestampRemove = intervalRemove.unixTime
            val dataRemove = CapturedData(
                timestampRemove,
                rssiRemove,
                RollingProximityIdentifier(rpiRemove, createFromUnixTimestamp(timestampRemove)),
                AssociatedEncryptedMetadata(aemRemove)
            )
            database.addCapturedPayload(dataRemove)
        }
        val rpiKeep = ByteArray(16)
        random.nextBytes(rpiKeep)
        val aemKeep = ByteArray(4)
        random.nextBytes(aemKeep)
        val rssiKeepArray = ByteArray(1)
        random.nextBytes(rssiKeepArray)
        val rssiKeep = rssiKeepArray[0]
        val timestampKeep = intervalKeep.unixTime
        val dataKeep = CapturedData(
            timestampKeep,
            rssiKeep,
            RollingProximityIdentifier(rpiKeep, intervalKeep),
            AssociatedEncryptedMetadata(aemKeep)
        )
        database.addCapturedPayload(dataKeep)

        // Truncate:
        database.truncateLast14Days()

        // Query:
        val resultTeks = database.getAllOwnTEKs()
        val resultIntervals = database.getAllCollectedPayload()

        // Compare:
        var numResultTeks = 0
        for (resultTek in resultTeks) {
            numResultTeks++
            assertArrayEquals(tekKeepBytes, resultTek.key)
            assertEquals(intervalKeep, resultTek.interval)
        }
        assertEquals(1, numResultTeks.toLong())
        var numResultIntervals = 0
        var numResultData = 0
        for (resultInterval in resultIntervals) {
            numResultIntervals++
            for (resultData in resultInterval.getCapturedData()) {
                numResultData++
                assertArrayEquals(rpiKeep, resultData.rpi.getData())
                assertArrayEquals(aemKeep, resultData.aem.data)
                assertEquals(rssiKeep.toLong(), resultData.rssi.toLong())
                assertEquals(timestampKeep, resultData.captureTimestampMillis)
            }
        }
        assertEquals(1, numResultIntervals.toLong())
        assertEquals(1, numResultData.toLong())
    }

    @Test
    fun testAddDiagnosisKeys() {
        val diagKeys = LinkedList<DiagnosisKey>()
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey())
        val token0 = "token0"
        database.addDiagnosisKeys(token0, diagKeys)
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey())
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey())
        val token1 = "token1"
        database.addDiagnosisKeys(token1, diagKeys)

        // First verify that insertion with different tokens works:
        run {
            val result0 = database.getDiagnosisKeys(token0)
            assertEquals(1, result0.size.toLong())
            val result1 = database.getDiagnosisKeys(token1)
            assertEquals(3, result1.size.toLong())
        }

        // Now add additional keys for an existing token:
        database.addDiagnosisKeys(token1, diagKeys)
        run {
            val result0 = database.getDiagnosisKeys(token0)
            assertEquals(1, result0.size.toLong())
            val result1 = database.getDiagnosisKeys(token1)
            assertEquals(6, result1.size.toLong())
        }
    }

    @Test
    fun testDeleteToken() {
        val diagKeys = LinkedList<DiagnosisKey>()
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey())
        val token0 = "token0"
        database.addDiagnosisKeys(token0, diagKeys)
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey())
        diagKeys.add(DatatypesTestUtil.createDummyDiagnosisKey())
        val token1 = "token1"
        database.addDiagnosisKeys(token1, diagKeys)

        // Verify insertion worked as expected:
        run {
            val result0 = database.getDiagnosisKeys(token0)
            assertEquals(1, result0.size.toLong())
            val result1 = database.getDiagnosisKeys(token1)
            assertEquals(3, result1.size.toLong())
        }

        // Now delete token1 and check again:
        database.deleteTokenWithData(token1)
        run {
            val result0 = database.getDiagnosisKeys(token0)
            assertEquals(1, result0.size.toLong())
            val result1 = database.getDiagnosisKeys(token1)
            assertEquals(0, result1.size.toLong())
        }
    }
}
