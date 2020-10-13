package org.coralibre.android.sdk.internal.crypto;

import android.annotation.SuppressLint;

import com.google.crypto.tink.subtle.Hkdf;

import org.coralibre.android.sdk.internal.database.Database;
import org.coralibre.android.sdk.internal.database.DatabaseAccess;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class CryptoModule {
    private static final String TAG = CryptoModule.class.getName();

    public static final int TEK_MAX_STORE_TIME = 14; //defined as days
    public static final int FUZZY_COMPARE_TIME_DEVIATION = 12; //defined int 10min units
    public static final String RPIK_INFO = "EN-RPIK";
    public static final String AEMK_INFO = "EN-AEMK";

    private static CryptoModule instance;
    private ENInterval currentTekDay = new ENInterval(0);
    private RollingProximityIdentifierKey currentRPIK;
    private AssociatedEncryptedMetadataKey currentAEMK;
    private BluetoothPayload currentPayload = null;
    private AssociatedMetadata metadata = null;
    private Database database;


    private boolean testMode;
    private ENInterval currentIntervalForTesting;

    public static CryptoModule getInstance() {
        //TODO: use proper factory class
        if (instance == null) {
            instance = new CryptoModule(DatabaseAccess.getDefaultDatabaseInstance());
        }
        return instance;
    }

    public CryptoModule(Database db) {
        testMode = false; // remove this line and everything goes BOOM!!!
        //TODO: Use proper dependency injection
        init(db, getCurrentInterval());
    }

    /**
     * If you call this method your warranty will be void.
     * This Construction is only supposed to be used during testing. If app is compiled
     * for production purpose this should be avoided.
     **/
    private CryptoModule(Database db, ENInterval interval) {
        currentIntervalForTesting = interval;
        testMode = true;
        init(db, interval);
    }

    //TODO: Use a factory for getting a crypto module in order to do proper dependency injection.
    private void init(Database db, ENInterval currentInterval) {
        try {
            database = db;

            ENInterval intervalNumberMidnight = TemporaryExposureKey_internal.getMidnight(currentInterval);
            if (! database.hasTEKForInterval(intervalNumberMidnight)) {
                updateTEK();
            } else {
                TemporaryExposureKey_internal tek = database.getOwnTEK(intervalNumberMidnight);
                currentTekDay = tek.getInterval();
                currentRPIK = generateRPIK(tek);
                currentAEMK = generateAEMK(tek);
            }
        } catch (Exception e) {
            throw new CryptoException("could not crate new CryptoModule instance", e);
        }
    }

    public static ENInterval getCurrentInterval() {
        return new ENInterval(System.currentTimeMillis() / 1000L, true);
    }

    private TemporaryExposureKey_internal getNewRandomTEK() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGenerator.generateKey();
            ENInterval now = testMode ? currentIntervalForTesting : getCurrentInterval();
            return new TemporaryExposureKey_internal(now, secretKey.getEncoded());
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    private static byte[] generateHKDFBytes(byte[] tek, byte[] info, int length) {
        try {
            return Hkdf.computeHkdf(
                    "HMACSHA256",
                    tek,
                    null,
                    info,
                    length
            );
        } catch (GeneralSecurityException e) {
            // Could only happen if MAC algorithm isn't supported or size is too big,
            // both of which shouldn't ever happen here.
            throw new RuntimeException(e);
        }
    }

    public static RollingProximityIdentifierKey generateRPIK(TemporaryExposureKey_internal tek) {
        return generateRPIK(tek.getKey());
    }

    public static RollingProximityIdentifierKey generateRPIK(byte[] tek) {
        byte[] rawRPIK = generateHKDFBytes(
            tek,
            RPIK_INFO.getBytes(StandardCharsets.UTF_8),
            RollingProximityIdentifierKey.RPIK_LENGTH
        );
        return new RollingProximityIdentifierKey(rawRPIK);
    }

    public static AssociatedEncryptedMetadataKey generateAEMK(TemporaryExposureKey_internal tek) {
        byte[] rawAEMK = generateHKDFBytes(
                tek.getKey(),
                AEMK_INFO.getBytes(StandardCharsets.UTF_8),
                AssociatedEncryptedMetadataKey.AEMK_LENGTH
        );
        return new AssociatedEncryptedMetadataKey(rawAEMK);
    }

    public RollingProximityIdentifier generateRPI(RollingProximityIdentifierKey rpik) {
        return generateRPI(rpik, testMode ? currentIntervalForTesting : getCurrentInterval());
    }

    public static RollingProximityIdentifier generateRPI(RollingProximityIdentifierKey rpik,
                                                         ENInterval interval) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(rpik.getKey(), "AES");
            // normally ECB is a bad idea, but in this case we just want to encrypt a single block
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            PaddedData paddedData = new PaddedData(interval);
            return new RollingProximityIdentifier(cipher.update(paddedData.getData()), interval);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static PaddedData decryptRPI(RollingProximityIdentifier rpi,
                                        RollingProximityIdentifierKey rpik) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(rpik.getKey(), "AES");
            // normally ECB is a bad idea, but in this case we just want to encrypt a single block
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            return new PaddedData(cipher.update(rpi.getData()));
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static AssociatedEncryptedMetadata encryptAM(AssociatedMetadata am,
                                                        RollingProximityIdentifier rpi,
                                                        AssociatedEncryptedMetadataKey aemk) {
        try {
            SecretKey keySpec = new SecretKeySpec(aemk.getKey(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(rpi.getData());
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            return new AssociatedEncryptedMetadata(cipher.update(am.getData()));
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static AssociatedMetadata decryptAEM(AssociatedEncryptedMetadata aem,
                                                RollingProximityIdentifier rpi,
                                                AssociatedEncryptedMetadataKey aemk) {
        try {
            SecretKey keySpec = new SecretKeySpec(aemk.getKey(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(rpi.getData());
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new AssociatedMetadata(cipher.update(aem.getData()));
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    public static AssociatedMetadata decryptAEM(AssociatedEncryptedMetadata aem,
                                                RollingProximityIdentifier rpi,
                                                TemporaryExposureKey_internal tek) {
        return decryptAEM(aem, rpi, generateAEMK(tek));
    }

    public void updateTEK() {
        System.out.println(TemporaryExposureKey_internal.getMidnight(testMode ? currentIntervalForTesting : getCurrentInterval()).get());
        if (!currentTekDay.equals(
                TemporaryExposureKey_internal.getMidnight(testMode ? currentIntervalForTesting : getCurrentInterval()))) {
            TemporaryExposureKey_internal currentTek = getNewRandomTEK();
            currentTekDay = currentTek.getInterval();
            currentRPIK = generateRPIK(currentTek);
            currentAEMK = generateAEMK(currentTek);

            Database database = DatabaseAccess.getDefaultDatabaseInstance();
            database.addGeneratedTEK(new TemporaryExposureKey_internal(currentTekDay, currentTek.getKey()));
        }
    }

    public void renewPayload() {
        if (metadata == null)
            throw new CryptoException("Associated metadata has not yet been set.");

        if (currentPayload == null
                || !currentPayload.getInterval().equals(testMode ? currentIntervalForTesting : getCurrentInterval())){
            updateTEK();
            RollingProximityIdentifier currentRPI = generateRPI(currentRPIK, testMode ? currentIntervalForTesting : getCurrentInterval());
            AssociatedEncryptedMetadata currentAEM = encryptAM(metadata, currentRPI, currentAEMK);
            currentPayload = new BluetoothPayload(currentRPI, currentAEM);
        }
    }

    public BluetoothPayload getCurrentPayload() {
        if (currentPayload == null)
            throw new CryptoException("You need to run renewPayload() before calling getCurrentPayload() for the first time.");
        return currentPayload;
    }

    public AssociatedMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssociatedMetadata metadata) {
        this.metadata = metadata;
    }
}
