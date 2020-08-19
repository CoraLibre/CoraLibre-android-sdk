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



}
