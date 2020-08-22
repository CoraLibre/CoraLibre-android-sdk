package org.coralibre.android.sdk.internal.database;

import android.content.Context;

public class DatabaseAccess {
    // Workaround class to enable easy switching between different database implementations.
    // Use this class whenever access to the database is required.

    // TODO: Use a proper factory for database creation.


    private static Database defaultDatabaseInstance;
    private static boolean isInitialized = false;


    private static void createDefaultDatabaseInstance(Context appContext) {
        // To change (default) database type, only change the following line:
        defaultDatabaseInstance = new PersistentDatabase(appContext, true);
            // TODO: Set inMemoryMock parameter to false to use real persistent database.
    }



    // Public interface:

    public static void init(Context appContext) {
        if (isInitialized) {
            throw new StorageException("Attempt to reinitialize database.");
        }
        createDefaultDatabaseInstance(appContext);
        isInitialized = true;
    }


    public static Database getDefaultDatabaseInstance() {
        if (!isInitialized) {
            throw new StorageException("Cannot access database. Database not initialized yet.");
        }
        return defaultDatabaseInstance;
    }


    /**
     * Clears the db field and sets the state to uninitialized. Only intended to be used after
     * unit tests, so that the next tests can initialize a fresh db without an exception being
     * thrown.
     */
    public static void deInit() {
        if (!isInitialized) {
            throw new StorageException("Cannot forget database that is not known/existent.");
        }
        defaultDatabaseInstance = null;
        isInitialized = false;
    }


}
