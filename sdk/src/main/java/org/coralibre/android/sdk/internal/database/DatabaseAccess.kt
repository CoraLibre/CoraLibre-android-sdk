package org.coralibre.android.sdk.internal.database

import android.content.Context

object DatabaseAccess {
    // Workaround class to enable easy switching between different database implementations.
    // Use this class whenever access to the database is required.
    // TODO: Use a proper factory for database creation.
    private var defaultDatabaseInstance: Database? = null
    private var isInitialized = false
    private fun createDefaultDatabaseInstance(appContext: Context) {
        // To change (default) database type, only change the following line:
        defaultDatabaseInstance = PersistentDatabase(appContext, true)
        // TODO: Set inMemoryMock parameter to false to use real persistent database.
    }

    // Public interface:
    @JvmStatic
    fun init(appContext: Context) {
        if (isInitialized) {
            throw StorageException("Attempt to reinitialize database.")
        }
        createDefaultDatabaseInstance(appContext)
        isInitialized = true
    }

    @JvmStatic
    fun getDefaultDatabaseInstance(): Database {
        if (!isInitialized) {
            throw StorageException("Cannot access database. Database not initialized yet.")
        }
        return defaultDatabaseInstance!!
    }

    /**
     * Clears the db field and sets the state to uninitialized. Only intended to be used after
     * unit tests, so that the next tests can initialize a fresh db without an exception being
     * thrown.
     */
    @JvmStatic
    fun deInit() {
        if (!isInitialized) {
            throw StorageException("Cannot forget database that is not known/existent.")
        }
        defaultDatabaseInstance = null
        isInitialized = false
    }
}
