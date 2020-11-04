package org.coralibre.android.sdk.internal.database.persistent

import androidx.room.TypeConverter
import org.coralibre.android.sdk.internal.datatypes.ENInterval

object ENIntervalConverter {
    @JvmStatic
    @TypeConverter
    fun toENNumber(value: Long): ENInterval {
        return ENInterval(value)
    }

    @JvmStatic
    @TypeConverter
    fun toLong(value: ENInterval): Long {
        return value.get()
    }
}
