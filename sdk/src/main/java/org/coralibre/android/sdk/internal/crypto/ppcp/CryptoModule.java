package org.coralibre.android.sdk.internal.crypto.ppcp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.crypto.tink.subtle.Hkdf;

import org.coralibre.android.sdk.internal.database.ppcp.Database;
import org.coralibre.android.sdk.internal.database.ppcp.MockDatabase;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEK;
import org.coralibre.android.sdk.internal.database.ppcp.model.GeneratedTEKImpl;

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

    private SharedPreferences esp;
    private ENNumber currentTekDay = new ENNumber(0);
    private RollingProximityIdentifierKey currentRPIK;
    private AssociatedEncryptedMetadataKey currentAEMK;
    private BluetoothPayload currentPayload = null;
    private AssociatedMetadata metadata = null;

    public static CryptoModule getInstance(Context context) {
        try {
            if (instance == null) {
                instance = new CryptoModule();
                String KEY_ALIAS = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                instance.esp = EncryptedSharedPreferences.create("coralibre_store",
                        KEY_ALIAS,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

                //TODO: use dependency injection
                Database database = MockDatabase.getInstance();
                GeneratedTEK rawTek = database.getGeneratedTEK(
                        TemporaryExposureKey.getMidnight(getCurrentInterval()));
                if(rawTek == null) {
                    instance.updateTEK();
                } else {
                    TemporaryExposureKey tek = new TemporaryExposureKey(rawTek.getInterval(), rawTek.getKey());
                    instance.currentTekDay = tek.getInterval();
                    instance.currentRPIK = generateRPIK(tek);
                    instance.currentAEMK = generateAEMK(tek);
                }

            }
            return instance;
        } catch(Exception e) {
            throw new CryptoException("could not crate new CryptoModule instance", e);
        }
    }

    public static ENNumber getCurrentInterval() {
        return new ENNumber(System.currentTimeMillis() / 1000L, true);
    }

    private static TemporaryExposureKey getNewRandomTEK() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGenerator.generateKey();
            ENNumber now = getCurrentInterval();
            return new TemporaryExposureKey(now, secretKey.getEncoded());
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    private static byte[] generateHKDFBytes(TemporaryExposureKey tek, byte[] info, int length) {
        try {
            return Hkdf.computeHkdf(
                    "HMACSHA256",
                    tek.getKey(),
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

    public static RollingProximityIdentifierKey generateRPIK(TemporaryExposureKey tek) {
        byte[] rawRPIK = generateHKDFBytes(
                tek,
                RPIK_INFO.getBytes(StandardCharsets.UTF_8),
                RollingProximityIdentifierKey.RPIK_LENGTH
        );
        return new RollingProximityIdentifierKey(rawRPIK);
    }

    public static AssociatedEncryptedMetadataKey generateAEMK(TemporaryExposureKey tek) {
        byte[] rawAEMK = generateHKDFBytes(
                tek,
                AEMK_INFO.getBytes(StandardCharsets.UTF_8),
                AssociatedEncryptedMetadataKey.AEMK_LENGTH
        );
        return new AssociatedEncryptedMetadataKey(rawAEMK);
    }

    public static RollingProximityIdentifier generateRPI(RollingProximityIdentifierKey rpik) {
        return generateRPI(rpik, getCurrentInterval());
    }

    public static RollingProximityIdentifier generateRPI(RollingProximityIdentifierKey rpik,
                                                         ENNumber interval) {
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
                                                TemporaryExposureKey tek) {
        return decryptAEM(aem, rpi, generateAEMK(tek));
    }

    private void updateTEK() {
        if(!currentTekDay.equals(
                TemporaryExposureKey.getMidnight(getCurrentInterval()))) {
            TemporaryExposureKey currentTek = getNewRandomTEK();
            currentTekDay = currentTek.getInterval();
            currentRPIK = generateRPIK(currentTek);
            currentAEMK = generateAEMK(currentTek);

            //TODO: use dependency injection
            Database database = MockDatabase.getInstance();
            database.addGeneratedTEK(new GeneratedTEKImpl(currentTekDay, currentTek.getKey()));
        }
    }

    public BluetoothPayload getCurrentPayload() {
        if(metadata == null) throw new CryptoException("Associated metadata has not yet been set.");

        if(currentPayload == null
                || !currentPayload.getInterval().equals(getCurrentInterval())) {
            updateTEK();
            RollingProximityIdentifier currentRPI = generateRPI(currentRPIK, getCurrentInterval());
            AssociatedEncryptedMetadata currentAEM = encryptAM(metadata, currentRPI, currentAEMK);
            currentPayload = new BluetoothPayload(currentRPI, currentAEM);
        }

        return currentPayload;
    }

    public AssociatedMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssociatedMetadata metadata) {
        this.metadata = metadata;
    }
}
