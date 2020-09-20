package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.TypeConverter;

import org.coralibre.android.sdk.internal.crypto.ENInterval;

public class ENIntervalConverter {

    @TypeConverter
    public static ENInterval toENNumber(Long value) {
        return new ENInterval(value);
    }

    @TypeConverter
    public static Long toLong(ENInterval value) {
        return value.get();
    }

}
