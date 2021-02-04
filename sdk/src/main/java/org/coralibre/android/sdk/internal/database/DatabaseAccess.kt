package org.coralibre.android.sdk.internal.database

import android.content.Context

object DatabaseAccess {
    // Workaround class to enable easy switching between different database implementations.
    // Use this class whenever access to the database is required.
    // TODO: Use a proper factory for database creation.
    private var defaultDatabaseInstance: Database? = null

    @Synchronized
    @JvmStatic
    fun init(appContext: Context) {
        if (defaultDatabaseInstance == null) {
            // To change (default) database type, only change the following line:
            defaultDatabaseInstance = PersistentDatabase(appContext, false)
        } else {
            throw StorageException("Attempt to reinitialize database.")
        }
    }

    @JvmStatic
    fun getDefaultDatabaseInstance(): Database {
        return defaultDatabaseInstance
            ?: throw StorageException("Cannot access database. Database not initialized yet.")
    }

    /**
     * Clears the db field and sets the state to uninitialized. Only intended to be used after
     * unit tests, so that the next tests can initialize a fresh db without an exception being
     * thrown.
     */
    @Synchronized
    @JvmStatic
    fun deInit() {
        if (defaultDatabaseInstance == null) {
            throw StorageException("Cannot forget database that is not known/existent.")
        }
        defaultDatabaseInstance = null
    }
}
