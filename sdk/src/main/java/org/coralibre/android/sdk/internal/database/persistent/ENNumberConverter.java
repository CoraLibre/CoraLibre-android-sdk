package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.TypeConverter;

import org.coralibre.android.sdk.internal.crypto.ENNumber;

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
