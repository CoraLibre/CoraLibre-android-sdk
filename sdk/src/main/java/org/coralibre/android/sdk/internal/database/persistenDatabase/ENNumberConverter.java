package org.coralibre.android.sdk.internal.database.persistenDatabase;

import androidx.room.TypeConverter;

import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;

public class ENNumberConverter {

    @TypeConverter
    public static ENNumber toENNumber(Long value) {
        return new ENNumber(value);
    }

    @TypeConverter
    public static Long toLong(ENNumber value) {
        return value.get();
    }

}
