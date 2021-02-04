package org.coralibre.android.sdk.internal

import android.content.Context
import android.content.Intent
import android.util.Log
import org.coralibre.android.sdk.BuildConfig
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureConfiguration
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureNotificationClient
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey
import org.coralibre.android.sdk.internal.database.DatabaseAccess
import org.coralibre.android.sdk.internal.datatypes.DiagnosisKey
import org.coralibre.android.sdk.internal.datatypes.IntervalOfCapturedData
import org.coralibre.android.sdk.internal.datatypes.util.DiagnosisKeyUtil.toDiagnosisKeys
import org.coralibre.android.sdk.internal.matching.MatchingLegacyV1.hasMatches
import org.coralibre.android.sdk.proto.TemporaryExposureKeyFile.TemporaryExposureKeyExport
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.LinkedList
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal object CoraLibre {
    // TODO: document what this does
    const val UPDATE_INTENT_ACTION = "org.coralibre.android.sdk.UPDATE_ACTION"

    private const val TAG = "CoraLibre"
    private var isInitialized = false

    private fun init(context: Context) {
        DatabaseAccess.init(context)
        // TODO: Schedule the truncation to happen regularly instead of (only) performing it here.
        DatabaseAccess.getDefaultDatabaseInstance().truncateLast14Days()
        isInitialized = true
    }

    @JvmStatic
    @Synchronized
    fun checkInit(context: Context) {
        if (!isInitialized) {
            init(context)
        }
    }

    @JvmStatic
    fun enable(context: Context) {
        checkInit(context)
        val appConfigManager = AppConfigManager.getInstance(context)
        appConfigManager.isAdvertisingEnabled = true
        appConfigManager.isReceivingEnabled = true
        TracingService.startService(context)
        BroadcastHelper.sendUpdateBroadcast(context)
    }

    @JvmStatic
    fun isEnabled(context: Context): Boolean {
        val appConfigManager = AppConfigManager.getInstance(context)
        return appConfigManager.isAdvertisingEnabled || appConfigManager.isReceivingEnabled
    }

    @JvmStatic
    fun getTemporaryExposureKeyHistory(context: Context): List<TemporaryExposureKey> {
        checkInit(context)

        val result: MutableList<TemporaryExposureKey> = LinkedList()
        val database = DatabaseAccess.getDefaultDatabaseInstance()
        for (tek in database.getAllOwnTEKs()) {
            result.add(
                TemporaryExposureKey(
                    tek.key,
                    tek.interval.get().toInt(),
                    EnFrameworkConstants.TEK_ROLLING_PERIOD,
                    // TODO Means "Unused"; is this correct here? verify, that the CWA sets this value before uploading
                    0,
                    // TODO this means "UNKNOWN"; is this correct here? see https://developers.google.com/android/exposure-notifications/exposure-notifications-api
                    0
                )
            )
        }
        return result
    }

    @JvmStatic
    fun stop(context: Context) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Stopping CoraLibre")
        }
        checkInit(context)
        val intent = Intent(context, TracingService::class.java)
            .setAction(TracingService.ACTION_STOP)
        context.startService(intent)
        val appConfigManager = AppConfigManager.getInstance(context)
        appConfigManager.isAdvertisingEnabled = false
        appConfigManager.isReceivingEnabled = false
        BroadcastHelper.sendUpdateBroadcast(context)
    }

    @JvmStatic
    fun provideDiagnosisKeys(
        context: Context,
        keyFiles: MutableList<File>,
        exposureConfiguration: ExposureConfiguration?,
        token: String
    ) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Handling provided diagnosis keys")
        checkInit(context)
        val database = DatabaseAccess.getDefaultDatabaseInstance()
        // TODO: Move method outside of this file
        // TODO save exposure configuration to database, to restore it after e.g. phone restart, like microg?
        for (file in keyFiles) {
            try {
                ZipInputStream(BufferedInputStream(FileInputStream(file))).use { stream ->
                    var zipEntry: ZipEntry?
                    while (stream.nextEntry.also { zipEntry = it } != null) {
                        if (zipEntry!!.name == "export.bin") {
                            val prefix = ByteArray(16)
                            var totalBytesRead = 0
                            var bytesRead = 0
                            while (bytesRead != -1 && totalBytesRead < prefix.size) {
                                bytesRead = stream.read(
                                    prefix,
                                    totalBytesRead,
                                    prefix.size - totalBytesRead
                                )
                                if (bytesRead > 0) {
                                    totalBytesRead += bytesRead
                                }
                            }
                            val prefixString = String(prefix).trim { it <= ' ' }
                            if (totalBytesRead == prefix.size && prefixString == "EK Export v1") {
                                val temporaryExposureKeyExport =
                                    TemporaryExposureKeyExport.parseFrom(stream)
                                database.addDiagnosisKeys(
                                    token,
                                    toDiagnosisKeys(temporaryExposureKeyExport.keysList)
                                )
                                database.updateDiagnosisKeys(
                                    token,
                                    toDiagnosisKeys(temporaryExposureKeyExport.revisedKeysList)
                                )
                                // TODO verify that first add and then update is correct here
                            } else {
                                Log.e(
                                    TAG,
                                    "Failed to parse diagnosis key file: export.bin has invalid prefix: $prefixString"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse diagnosis key file", e)
            }
        }

        // TODO Discard keys older than 14 days
        //  See description of method "provideDiagnosisKeys()" in:
        //  https://developers.google.com/android/exposure-notifications/exposure-notifications-api
        //  "Keys provided with the same token accumulate into the same set, and are aged out
        //  of those sets as they pass out of the 14-day window."

        // TODO Are measurements from today used for the matching? If not, remove them before
        //  testing for matches (same for the ExposureSummary/ExposureInformation computation)

        // TODO Discard keys older than 14 days
        //  See description of method "provideDiagnosisKeys()" in:
        //  https://developers.google.com/android/exposure-notifications/exposure-notifications-api
        //  "Keys provided with the same token accumulate into the same set, and are aged out
        //  of those sets as they pass out of the 14-day window."

        // TODO Are measurements from today used for the matching? If not, remove them before
        //  testing for matches (same for the ExposureSummary/ExposureInformation computation)
        val diagnosisKeys: List<DiagnosisKey> = database.getDiagnosisKeys(token)
        val capturedData: Iterable<IntervalOfCapturedData> = database.getAllCollectedPayload()
        val noMatchFound = !hasMatches(diagnosisKeys, capturedData)

        val intent = Intent().apply {
            action = if (noMatchFound) ExposureNotificationClient.ACTION_EXPOSURE_NOT_FOUND
            else ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
            putExtra(ExposureNotificationClient.EXTRA_TOKEN, token)
        }
        context.sendOrderedBroadcast(intent, null)
    }
}
