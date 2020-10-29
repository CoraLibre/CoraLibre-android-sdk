package org.coralibre.android.sdk.internal.crypto;

import android.util.Pair;

import org.coralibre.android.sdk.internal.EnFrameworkConstants;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifierKey;
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey;
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;

import java.util.ArrayList;
import java.util.List;

import static org.coralibre.android.sdk.internal.crypto.CryptoModule.FUZZY_COMPARE_TIME_DEVIATION;
import static org.coralibre.android.sdk.internal.crypto.CryptoModule.generateRPI;
import static org.coralibre.android.sdk.internal.crypto.CryptoModule.generateRPIK;

public class ExposeChecker {
    public static List<RollingProximityIdentifier> generateAllRPIForADay(InternalTemporaryExposureKey tek) {
        final long enInterval = tek.getInterval().get();
        List<RollingProximityIdentifier> rpiList =
                new ArrayList<>(EnFrameworkConstants.TEK_ROLLING_PERIOD);
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        for(long i = 0; i < EnFrameworkConstants.TEK_ROLLING_PERIOD; i++) {
            rpiList.add(generateRPI(rpik, new ENInterval(enInterval + i)));
        }
        return rpiList;
    }


    private static List<InternalTemporaryExposureKey> getMatchingTEKs(List<InternalTemporaryExposureKey> allTEKs,
                                                                      ENInterval interval) {
        List<InternalTemporaryExposureKey> relatedTEKs = new ArrayList<>();
        for(InternalTemporaryExposureKey key : allTEKs) {
            if(key.getInterval().equals(interval)) {
                relatedTEKs.add(key);
            }
        }
        return relatedTEKs;
    }

    public static List<InternalTemporaryExposureKey> getAllRelatedTEKs(List<InternalTemporaryExposureKey> allTEKs,
                                                                       ENInterval interval) {
        ENInterval slotBeginning = ENIntervalUtil.getMidnight(
                new ENInterval(interval.get() - FUZZY_COMPARE_TIME_DEVIATION));
        ENInterval slotEnding = ENIntervalUtil.getMidnight(
                new ENInterval(interval.get() + FUZZY_COMPARE_TIME_DEVIATION));
        List<InternalTemporaryExposureKey> relatedTeKs = getMatchingTEKs(allTEKs, slotBeginning);
        if(!slotBeginning.equals(slotEnding)) {
            relatedTeKs.addAll(getMatchingTEKs(allTEKs, slotEnding));
        }
        return relatedTeKs;
    }

    public static List<RollingProximityIdentifier> generateRPIsForSlot(InternalTemporaryExposureKey tek,
                                                                       ENInterval interval) {
        long slotBeginning = interval.get() - FUZZY_COMPARE_TIME_DEVIATION;
        if(slotBeginning < tek.getInterval().get()) {
            slotBeginning = tek.getInterval().get();
        }
        long slotEnding = interval.get() + FUZZY_COMPARE_TIME_DEVIATION;
        if(slotEnding > tek.getInterval().get() + EnFrameworkConstants.TEK_ROLLING_PERIOD) {
            slotEnding = tek.getInterval().get() + EnFrameworkConstants.TEK_ROLLING_PERIOD;
        }
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        List<RollingProximityIdentifier> generatedRPIs =
                new ArrayList<>(2 * FUZZY_COMPARE_TIME_DEVIATION + 1);

        for(long i = slotBeginning; i <= slotEnding; i++) {
            generatedRPIs.add(generateRPI(rpik, new ENInterval(i)));
        }
        return generatedRPIs;
    }



    public static List<Pair<InternalTemporaryExposureKey, RollingProximityIdentifier>>
        findMatches(List<InternalTemporaryExposureKey> teks,
                    List<RollingProximityIdentifier> collectedRPIs) {
        //TODO: Do dynamic programing foo and use a cache
        List<Pair<InternalTemporaryExposureKey, RollingProximityIdentifier>> matchingKeys = new
                ArrayList<>();
        for(RollingProximityIdentifier crpi : collectedRPIs) {
            List<InternalTemporaryExposureKey> relatedTeks
                    = getAllRelatedTEKs(teks, crpi.getInterval());
            for(InternalTemporaryExposureKey tek : relatedTeks) {
                List<RollingProximityIdentifier> generatedRPIs
                        = generateRPIsForSlot(tek, crpi.getInterval());
                for(RollingProximityIdentifier grpi :  generatedRPIs) {
                    if(grpi.equals(crpi)) {
                        matchingKeys.add(new Pair<>(tek, crpi));
                    }
                }
            }
        }
        return matchingKeys;
    }
}
