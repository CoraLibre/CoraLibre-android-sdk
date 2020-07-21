package org.coralibre.android.sdk.internal.database;

import android.content.Context;

public class DatabaseAccess {
    // Workaround class to enable easy switching between different database implementations.
    // Use this class whenever access to the database is required.


    private static Database defaultDatabaseInstance;
    private static boolean initialized = false;


    private static void createDefaultDatabaseInstance(Context appContext) {
        // To change (default) database type, only change the following line:

        //defaultDatabaseInstance = new MockDatabase();
        defaultDatabaseInstance = new PersistentDatabase(appContext, true);
    }



    // Public interface:

    public static void init(Context appContext) {
        if (initialized) {
            throw new StorageException("Attempt to reinitialize database.");
        }
        createDefaultDatabaseInstance(appContext);
        initialized = true;
    }


    public static Database getDefaultDatabaseInstance() {
        if (!initialized) {
            throw new StorageException("Cannot access database. Database not initialized yet.");
        }
        return defaultDatabaseInstance;
    }



}
