package org.coralibre.android.sdk.fakegms.nearby;

import org.coralibre.android.sdk.internal.crypto.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.crypto.RollingProximityIdentifierKey;
import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.database.DatabaseAccess;
import org.coralibre.android.sdk.internal.database.model.CapturedData;
import org.coralibre.android.sdk.internal.database.model.DiagnosisKey;
import org.coralibre.android.sdk.internal.database.model.IntervalOfCapturedData;

import java.util.LinkedList;
import java.util.List;

public class IdentifyMatchesFromDb {

    // This is just a temporary glue class, which needs some refactoring after
    // the tracing and matching works / after we have a working mvp.


    public static void identifyMatches() throws Exception {

        Database db = DatabaseAccess.getDefaultDatabaseInstance();

        List<DiagnosisKey> diagnosisKeys = db.getAllDiagnosisKeys();

        Iterable<IntervalOfCapturedData> payloadIntevals = db.getAllCollectedPayload();
        for (DiagnosisKey diagKey : diagnosisKeys) {
            RollingProximityIdentifierKey rpik = CryptoModule.generateRPIK(diagKey.getKeyData());
            for (IntervalOfCapturedData interval : payloadIntevals) {
                RollingProximityIdentifier rpi = CryptoModule.generateRPI(rpik, interval.getInterval());
                for (CapturedData capturedData : interval.getCapturedData()) {

                    //capturedData.

                }
            }
        }

    }


}
