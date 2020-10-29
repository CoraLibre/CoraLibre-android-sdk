package org.coralibre.android.sdk;

import org.coralibre.android.sdk.internal.EnFrameworkConstants;
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata;
import org.coralibre.android.sdk.internal.datatypes.CapturedData;
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey;
import org.coralibre.android.sdk.internal.datatypes.ENInterval;
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey;
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil;

import java.util.Random;

public class DatatypesTestUtil {

    public static DiagnosisKey createDummyDiagnosisKey() {
        Random random = new Random();

        byte[] keyData = new byte[EnFrameworkConstants.TEK_LENGTH];
        random.nextBytes(keyData);
        long intervalNumber = ENIntervalUtil.getCurrentInterval().get();
        int transmissionRiskLevel = 0;

        return new DiagnosisKey(
            new InternalTemporaryExposureKey(intervalNumber, keyData),
            transmissionRiskLevel
        );
    }

    public static CapturedData createDummyCapturedData(ENInterval interval) {
        Random random = new Random();

        byte[] rpi = new byte[EnFrameworkConstants.RPI_LENGTH];
        random.nextBytes(rpi);
        byte[] aem = new byte[EnFrameworkConstants.AEM_LENGTH];
        random.nextBytes(aem);
        byte[] rssiArray = new byte[1];
        random.nextBytes(rssiArray);
        byte rssi = rssiArray[0];
        Long timestampKeep = interval.getUnixTime();

        return new CapturedData(
            timestampKeep,
            rssi,
            new RollingProximityIdentifier(rpi, interval),
            new AssociatedEncryptedMetadata(aem)
        );
    }

    public static InternalTemporaryExposureKey createDummyTemporaryExposureKey(ENInterval interval) {
        Random random = new Random();

        byte[] dumTekBytes = new byte[16];
        random.nextBytes(dumTekBytes);

        return new InternalTemporaryExposureKey(interval, dumTekBytes);
    }

    public static InternalTemporaryExposureKey createDummyTemporaryExposureKey() {
        Random random = new Random();

        byte[] dumTekBytes = new byte[16];
        random.nextBytes(dumTekBytes);

        return new InternalTemporaryExposureKey(ENIntervalUtil.getCurrentInterval(), dumTekBytes);
    }

}
