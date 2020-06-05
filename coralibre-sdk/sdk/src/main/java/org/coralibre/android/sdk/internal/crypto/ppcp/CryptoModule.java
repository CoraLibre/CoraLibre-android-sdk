package org.coralibre.android.sdk.internal.crypto.ppcp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.coralibre.android.sdk.internal.util.Json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoModule {
    private static final String TAG = CryptoModule.class.getName();

    public static final int TEK_MAX_STORE_TIME = 14; //defined as days
    public static final String RPIK_INFO = "EN-RPIK";
    public static final String AEMK_INFO = "EN-AEMK";

    private static final String TEK_LIST_JSON = "TEK_LIST_JSON";
    private static final String RPIAEM_TODAY_JSON = "RPIAEM_TODAY_JSON";

    private static CryptoModule instance;

    private SharedPreferences esp;

    public static CryptoModule getInstance(Context context) throws IOException, GeneralSecurityException {
        if(instance == null) {
            instance = new CryptoModule();
            String KEY_ALIAS = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            instance.esp = EncryptedSharedPreferences.create("coralibre_store",
                    KEY_ALIAS,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        }
        return instance;
    }

    protected RawTeKList getTEKList() {
        String tekListJson = esp.getString(TEK_LIST_JSON, null);
        return Json.safeFromJson(tekListJson, RawTeKList.class, RawTeKList::new);
    }

    private void storeTeKList(RawTeKList tekListRaw) {
        esp.edit().putString(TEK_LIST_JSON, Json.toJson(tekListRaw)).apply();
    }

    public static ENNumber getCurrentENNumber() {
        return new ENNumber(System.currentTimeMillis() / 1000L, true);
    }

    public static TemporaryExposureKey getNewRandomKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        SecretKey secretKey = keyGenerator.generateKey();
        ENNumber now = getCurrentENNumber();
        return new TemporaryExposureKey(now, secretKey.getEncoded());
    }

    public static RollingProximityIdentifierKey generateRPIK(TemporaryExposureKey tek)
            throws UnsupportedEncodingException {
        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA256Digest());
        generator.init(new HKDFParameters(tek.getKey(),
                null,
                RPIK_INFO.getBytes(StandardCharsets.UTF_8)));
        byte[] rawRPIK = new byte[RollingProximityIdentifierKey.RPIK_LENGTH];
        generator.generateBytes(rawRPIK, 0, RollingProximityIdentifierKey.RPIK_LENGTH);
        return new RollingProximityIdentifierKey(rawRPIK);
    }

    public static AssociatedEncryptedMetadataKey generateAEMK(TemporaryExposureKey tek)
            throws UnsupportedEncodingException {
        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA256Digest());
        generator.init(new HKDFParameters(tek.getKey(),
                null,
                AEMK_INFO.getBytes(StandardCharsets.UTF_8)));
        byte[] rawAEMK = new byte[AssociatedEncryptedMetadataKey.AEMK_LENGTH];
        generator.generateBytes(rawAEMK, 0, AssociatedEncryptedMetadataKey.AEMK_LENGTH);
        return new AssociatedEncryptedMetadataKey(rawAEMK);
    }

    public static RollingProximityIdentifier generateRPI(RollingProximityIdentifierKey rpik)
            throws InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException {
        SecretKeySpec keySpec = new SecretKeySpec(rpik.getKey(), "AES");
        // normally ECB is a bad idea, but in this case we just want to encrypt a single block
        @SuppressLint("GetInstance")
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        PaddedData paddedData = new PaddedData(getCurrentENNumber());
        return new RollingProximityIdentifier(cipher.update(paddedData.getData()));
    }

    public static PaddedData decryptRPI(RollingProximityIdentifier rpi,
                                        RollingProximityIdentifierKey rpik)
            throws InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException {
        SecretKeySpec keySpec = new SecretKeySpec(rpik.getKey(), "AES");
        // normally ECB is a bad idea, but in this case we just want to encrypt a single block
        @SuppressLint("GetInstance")
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        return new PaddedData(cipher.update(rpi.getData()));
    }

    public static AssociatedEncryptedMetadata encryptAM(AssociatedMetadata am,
                                                     RollingProximityIdentifier rpi,
                                                     AssociatedEncryptedMetadataKey aemk)
            throws InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        SecretKey keySpec = new SecretKeySpec(aemk.getKey(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(rpi.getData());
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return new AssociatedEncryptedMetadata(cipher.update(am.getData()));
    }

    public static AssociatedMetadata decryptAEM(AssociatedEncryptedMetadata aem,
                                                RollingProximityIdentifier rpi,
                                                AssociatedEncryptedMetadataKey aemk)
            throws InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException{
        SecretKey keySpec = new SecretKeySpec(aemk.getKey(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(rpi.getData());
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        return new AssociatedMetadata(cipher.update(aem.getData()));
    }
}
