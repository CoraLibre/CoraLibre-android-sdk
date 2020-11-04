package org.coralibre.android.sdk.internal.crypto

import android.util.Pair
import org.coralibre.android.sdk.internal.EnFrameworkConstants
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.getMidnight
import java.util.ArrayList

object ExposeChecker {
    @JvmStatic
    fun generateAllRPIForADay(tek: InternalTemporaryExposureKey): List<RollingProximityIdentifier> {
        val enInterval = tek.interval.get()
        val rpiList = ArrayList<RollingProximityIdentifier>(EnFrameworkConstants.TEK_ROLLING_PERIOD)
        val rpik = CryptoModule.generateRPIK(tek)
        for (i in 0 until EnFrameworkConstants.TEK_ROLLING_PERIOD) {
            rpiList.add(CryptoModule.generateRPI(rpik, ENInterval(enInterval + i)))
        }
        return rpiList
    }

    private fun getMatchingTEKs(
        allTEKs: List<InternalTemporaryExposureKey>,
        interval: ENInterval
    ): MutableList<InternalTemporaryExposureKey> {
        val relatedTEKs: MutableList<InternalTemporaryExposureKey> = ArrayList()
        for (key in allTEKs) {
            if (key.interval.equals(interval)) {
                relatedTEKs.add(key)
            }
        }
        return relatedTEKs
    }

    @JvmStatic
    fun getAllRelatedTEKs(
        allTEKs: List<InternalTemporaryExposureKey>,
        interval: ENInterval
    ): List<InternalTemporaryExposureKey> {
        val slotBeginning = getMidnight(
            ENInterval(interval.get() - CryptoModule.FUZZY_COMPARE_TIME_DEVIATION)
        )
        val slotEnding = getMidnight(
            ENInterval(interval.get() + CryptoModule.FUZZY_COMPARE_TIME_DEVIATION)
        )
        val relatedTeKs = getMatchingTEKs(allTEKs, slotBeginning)
        if (slotBeginning != slotEnding) {
            relatedTeKs.addAll(getMatchingTEKs(allTEKs, slotEnding))
        }
        return relatedTeKs
    }

    @JvmStatic
    fun generateRPIsForSlot(
        tek: InternalTemporaryExposureKey,
        interval: ENInterval
    ): List<RollingProximityIdentifier> {
        var slotBeginning = interval.get() - CryptoModule.FUZZY_COMPARE_TIME_DEVIATION
        if (slotBeginning < tek.interval.get()) {
            slotBeginning = tek.interval.get()
        }
        var slotEnding = interval.get() + CryptoModule.FUZZY_COMPARE_TIME_DEVIATION
        if (slotEnding > tek.interval.get() + EnFrameworkConstants.TEK_ROLLING_PERIOD) {
            slotEnding = tek.interval.get() + EnFrameworkConstants.TEK_ROLLING_PERIOD
        }
        val rpik = CryptoModule.generateRPIK(tek)
        val generatedRPIs: MutableList<RollingProximityIdentifier> =
            ArrayList(2 * CryptoModule.FUZZY_COMPARE_TIME_DEVIATION + 1)
        for (i in slotBeginning..slotEnding) {
            generatedRPIs.add(CryptoModule.generateRPI(rpik, ENInterval(i)))
        }
        return generatedRPIs
    }

    @JvmStatic
    fun findMatches(
        teks: List<InternalTemporaryExposureKey>,
        collectedRPIs: List<RollingProximityIdentifier>
    ): List<Pair<InternalTemporaryExposureKey, RollingProximityIdentifier>> {
        // TODO: Do dynamic programing foo and use a cache
        val matchingKeys: MutableList<Pair<InternalTemporaryExposureKey, RollingProximityIdentifier>> =
            ArrayList()
        for (crpi in collectedRPIs) {
            val relatedTeks = getAllRelatedTEKs(teks, crpi.interval)
            for (tek in relatedTeks) {
                val generatedRPIs = generateRPIsForSlot(tek, crpi.interval)
                for (grpi in generatedRPIs) {
                    if (grpi.equals(crpi)) {
                        matchingKeys.add(Pair(tek, crpi))
                    }
                }
            }
        }
        return matchingKeys
    }
}
