package org.coralibre.android.sdk.internal.crypto

import android.annotation.SuppressLint
import android.util.Log
import com.google.crypto.tink.subtle.Hkdf
import org.coralibre.android.sdk.internal.EnFrameworkConstants
import org.coralibre.android.sdk.internal.database.Database
import org.coralibre.android.sdk.internal.database.DatabaseAccess.getDefaultDatabaseInstance
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadata
import org.coralibre.android.sdk.internal.datatypes.AssociatedEncryptedMetadataKey
import org.coralibre.android.sdk.internal.datatypes.AssociatedMetadata
import org.coralibre.android.sdk.internal.datatypes.BluetoothPayload
import org.coralibre.android.sdk.internal.datatypes.ENInterval
import org.coralibre.android.sdk.internal.datatypes.InternalTemporaryExposureKey
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifier
import org.coralibre.android.sdk.internal.datatypes.RollingProximityIdentifierKey
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.currentInterval
import org.coralibre.android.sdk.internal.datatypes.util.ENIntervalUtil.getMidnight
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoModule private constructor(
    private val database: Database,
    private val testMode: Boolean,
) {
    private var currentTekDay = ENInterval(0)
    private var currentRPIK: RollingProximityIdentifierKey? = null
    private var currentAEMK: AssociatedEncryptedMetadataKey? = null
    private var currentPayload: BluetoothPayload? = null
    var metadata: AssociatedMetadata? = null

    private var currentIntervalForTesting: ENInterval? = null

    constructor(db: Database) : this(
        database = db,
        testMode = false
    ) {
        // TODO: Use proper dependency injection
        init(currentInterval)
    }

    /**
     * If you call this method your warranty will be void.
     * This Construction is only supposed to be used during testing. If app is compiled
     * for production purpose this should be avoided.
     */
    internal constructor(db: Database, interval: ENInterval) : this(
        database = db,
        testMode = true,
    ) {
        currentIntervalForTesting = interval
        init(interval)
    }

    // TODO: Use a factory for getting a crypto module in order to do proper dependency injection.
    private fun init(currentInterval: ENInterval) {
        try {
            val intervalNumberMidnight = getMidnight(currentInterval)
            if (!database.hasTEKForInterval(intervalNumberMidnight)) {
                updateTEK()
            } else {
                val tek = database.getOwnTEK(intervalNumberMidnight)
                currentTekDay = tek.interval
                currentRPIK = generateRPIK(tek)
                currentAEMK = generateAEMK(tek)
            }
        } catch (e: Exception) {
            throw CryptoException("could not crate new CryptoModule instance", e)
        }
    }

    private fun getNewRandomTEK(): InternalTemporaryExposureKey = try {
        val keyGenerator = KeyGenerator.getInstance("HmacSHA256")
        val secretKey = keyGenerator.generateKey()
        val now = if (testMode) currentIntervalForTesting else currentInterval
        InternalTemporaryExposureKey(now!!, secretKey.encoded)
    } catch (e: Exception) {
        throw CryptoException(e)
    }

    fun updateTEK() {
        val midnight = getMidnight((if (testMode) currentIntervalForTesting else currentInterval)!!)
        Log.d(TAG, "Midnight: ${midnight.get()}")
        if (currentTekDay != midnight) {
            val currentTek = getNewRandomTEK()
            currentTekDay = currentTek.interval
            currentRPIK = generateRPIK(currentTek)
            currentAEMK = generateAEMK(currentTek)
            database.addGeneratedTEK(InternalTemporaryExposureKey(currentTekDay, currentTek.key))
        }
    }

    fun renewPayload() {
        if (metadata == null) throw CryptoException("Associated metadata has not yet been set.")
        val currentInterval = if (testMode) currentIntervalForTesting else currentInterval
        val currentPayload = this.currentPayload
        if (currentPayload == null ||
            currentPayload.interval != currentInterval
        ) {
            updateTEK()
            val currentRPI = generateRPI(
                currentRPIK!!,
                currentInterval!!
            )
            val currentAEM = encryptAM(
                metadata!!, currentRPI, currentAEMK
            )
            this.currentPayload = BluetoothPayload(currentRPI, currentAEM)
        }
    }

    fun getCurrentPayload(): BluetoothPayload {
        return currentPayload ?: throw CryptoException(
            "You need to run renewPayload() before calling getCurrentPayload() for the first time."
        )
    }

    fun generateRPI(
        rpik: RollingProximityIdentifierKey
    ) = generateRPI(rpik, if (testMode) currentIntervalForTesting!! else currentInterval)

    companion object {
        private const val TAG = "CryptoModule"

        /**
         * Defined int 10min units.
         */
        const val FUZZY_COMPARE_TIME_DEVIATION = 12
        const val RPIK_INFO = "EN-RPIK"
        const val AEMK_INFO = "EN-AEMK"

        private var instance: CryptoModule? = null

        @Synchronized
        @JvmStatic
        fun getInstance(): CryptoModule {
            // TODO: use proper factory class
            if (instance == null) {
                instance = CryptoModule(getDefaultDatabaseInstance())
            }
            return instance!!
        }

        @JvmStatic
        private fun generateHKDFBytes(tek: ByteArray, info: ByteArray, length: Int): ByteArray {
            return try {
                Hkdf.computeHkdf(
                    "HMACSHA256",
                    tek,
                    null,
                    info,
                    length
                )
            } catch (e: GeneralSecurityException) {
                // Could only happen if MAC algorithm isn't supported or size is too big,
                // both of which shouldn't ever happen here.
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun generateRPIK(tek: InternalTemporaryExposureKey): RollingProximityIdentifierKey {
            return generateRPIK(tek.key)
        }

        @JvmStatic
        fun generateRPIK(tek: ByteArray): RollingProximityIdentifierKey {
            val rawRPIK = generateHKDFBytes(
                tek,
                RPIK_INFO.toByteArray(StandardCharsets.UTF_8),
                EnFrameworkConstants.RPIK_LENGTH
            )
            return RollingProximityIdentifierKey(rawRPIK)
        }

        @JvmStatic
        fun generateAEMK(tek: InternalTemporaryExposureKey): AssociatedEncryptedMetadataKey {
            val rawAEMK = generateHKDFBytes(
                tek.key,
                AEMK_INFO.toByteArray(StandardCharsets.UTF_8),
                EnFrameworkConstants.AEMK_LENGTH
            )
            return AssociatedEncryptedMetadataKey(rawAEMK)
        }

        @JvmStatic
        fun generateRPI(
            rpik: RollingProximityIdentifierKey,
            interval: ENInterval
        ): RollingProximityIdentifier {
            return try {
                val keySpec = SecretKeySpec(
                    rpik.key, "AES"
                )
                // normally ECB is a bad idea, but in this case we just want to encrypt a single block
                @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES/ECB/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec)
                val paddedData = PaddedData(interval)
                RollingProximityIdentifier(cipher.update(paddedData.getData()), interval)
            } catch (e: Exception) {
                throw CryptoException(e)
            }
        }

        @JvmStatic
        fun decryptRPI(
            rpi: RollingProximityIdentifier,
            rpik: RollingProximityIdentifierKey
        ): PaddedData {
            return try {
                val keySpec = SecretKeySpec(rpik.key, "AES")
                // normally ECB is a bad idea, but in this case we just want to encrypt a single block
                @SuppressLint("GetInstance") val cipher = Cipher.getInstance("AES/ECB/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, keySpec)
                PaddedData(cipher.update(rpi.getData()))
            } catch (e: Exception) {
                throw CryptoException(e)
            }
        }

        @JvmStatic
        fun encryptAM(
            am: AssociatedMetadata,
            rpi: RollingProximityIdentifier,
            aemk: AssociatedEncryptedMetadataKey?
        ): AssociatedEncryptedMetadata {
            return try {
                val keySpec: SecretKey = SecretKeySpec(aemk!!.key, "AES")
                val ivSpec = IvParameterSpec(rpi.getData())
                val cipher = Cipher.getInstance("AES/CTR/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
                AssociatedEncryptedMetadata(cipher.update(am.data))
            } catch (e: Exception) {
                throw CryptoException(e)
            }
        }

        @JvmStatic
        fun decryptAEM(
            aem: AssociatedEncryptedMetadata,
            rpi: RollingProximityIdentifier,
            aemk: AssociatedEncryptedMetadataKey
        ): AssociatedMetadata {
            return try {
                val keySpec: SecretKey = SecretKeySpec(aemk.key, "AES")
                val ivSpec = IvParameterSpec(rpi.getData())
                val cipher = Cipher.getInstance("AES/CTR/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                AssociatedMetadata(cipher.update(aem.data))
            } catch (e: Exception) {
                throw CryptoException(e)
            }
        }

        @JvmStatic
        fun decryptAEM(
            aem: AssociatedEncryptedMetadata,
            rpi: RollingProximityIdentifier,
            tek: InternalTemporaryExposureKey
        ): AssociatedMetadata {
            return decryptAEM(aem, rpi, generateAEMK(tek))
        }
    }
}
