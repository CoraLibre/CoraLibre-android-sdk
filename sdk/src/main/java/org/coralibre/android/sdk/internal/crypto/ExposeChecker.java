package org.coralibre.android.sdk.internal.crypto;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.coralibre.android.sdk.internal.crypto.CryptoModule.FUZZY_COMPARE_TIME_DEVIATION;
import static org.coralibre.android.sdk.internal.crypto.CryptoModule.generateRPI;
import static org.coralibre.android.sdk.internal.crypto.CryptoModule.generateRPIK;
import static org.coralibre.android.sdk.internal.crypto.TemporaryExposureKey_internal.TEK_ROLLING_PERIOD;
import static org.coralibre.android.sdk.internal.crypto.TemporaryExposureKey_internal.getMidnight;

public class ExposeChecker {
    public static List<RollingProximityIdentifier> generateAllRPIForADay(TemporaryExposureKey_internal tek) {
        final long enInterval = tek.getInterval().get();
        List<RollingProximityIdentifier> rpiList =
                new ArrayList<>(TemporaryExposureKey_internal.TEK_ROLLING_PERIOD);
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        for(long i = 0; i < TemporaryExposureKey_internal.TEK_ROLLING_PERIOD; i++) {
            rpiList.add(generateRPI(rpik, new ENInterval(enInterval + i)));
        }
        return rpiList;
    }


    private static List<TemporaryExposureKey_internal> getMatchingTEKs(List<TemporaryExposureKey_internal> allTEKs,
                                                                       ENInterval interval) {
        List<TemporaryExposureKey_internal> relatedTEKs = new ArrayList<>();
        for(TemporaryExposureKey_internal key : allTEKs) {
            if(key.getInterval().equals(interval)) {
                relatedTEKs.add(key);
            }
        }
        return relatedTEKs;
    }

    public static List<TemporaryExposureKey_internal> getAllRelatedTEKs(List<TemporaryExposureKey_internal> allTEKs,
                                                                        ENInterval interval) {
        ENInterval slotBeginning = getMidnight(
                new ENInterval(interval.get() - FUZZY_COMPARE_TIME_DEVIATION));
        ENInterval slotEnding = getMidnight(
                new ENInterval(interval.get() + FUZZY_COMPARE_TIME_DEVIATION));
        List<TemporaryExposureKey_internal> relatedTeKs = getMatchingTEKs(allTEKs, slotBeginning);
        if(!slotBeginning.equals(slotEnding)) {
            relatedTeKs.addAll(getMatchingTEKs(allTEKs, slotEnding));
        }
        return relatedTeKs;
    }

    public static List<RollingProximityIdentifier> generateRPIsForSlot(TemporaryExposureKey_internal tek,
                                                                       ENInterval interval) {
        long slotBeginning = interval.get() - FUZZY_COMPARE_TIME_DEVIATION;
        if(slotBeginning < tek.getInterval().get()) {
            slotBeginning = tek.getInterval().get();
        }
        long slotEnding = interval.get() + FUZZY_COMPARE_TIME_DEVIATION;
        if(slotEnding > tek.getInterval().get() + TEK_ROLLING_PERIOD) {
            slotEnding = tek.getInterval().get() + TEK_ROLLING_PERIOD;
        }
        RollingProximityIdentifierKey rpik = generateRPIK(tek);
        List<RollingProximityIdentifier> generatedRPIs =
                new ArrayList<>(2 * FUZZY_COMPARE_TIME_DEVIATION + 1);

        for(long i = slotBeginning; i <= slotEnding; i++) {
            generatedRPIs.add(generateRPI(rpik, new ENInterval(i)));
        }
        return generatedRPIs;
    }



    public static List<Pair<TemporaryExposureKey_internal, RollingProximityIdentifier>>
        findMatches(List<TemporaryExposureKey_internal> teks,
                    List<RollingProximityIdentifier> collectedRPIs) {
        //TODO: Do dynamic programing foo and use a cache
        List<Pair<TemporaryExposureKey_internal, RollingProximityIdentifier>> matchingKeys = new
                ArrayList<>();
        for(RollingProximityIdentifier crpi : collectedRPIs) {
            List<TemporaryExposureKey_internal> relatedTeks
                    = getAllRelatedTEKs(teks, crpi.getInterval());
            for(TemporaryExposureKey_internal tek : relatedTeks) {
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
