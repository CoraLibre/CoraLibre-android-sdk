package org.coralibre.android.sdk.internal.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import org.coralibre.android.sdk.R

enum class Permission(
    val permission: String,
    @get:StringRes val displayName: Int,
    @get:StringRes val description: Int,
) : Parcelable {
    AccessFineLocation(
        ACCESS_FINE_LOCATION,
        R.string.permission_name_fine_location,
        R.string.permission_description_fine_location,
    ),
    ;

    fun isGranted(context: Context): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    companion object CREATOR : Parcelable.Creator<Permission> {
        override fun createFromParcel(source: Parcel): Permission {
            return values()[source.readInt()]
        }

        override fun newArray(size: Int): Array<Permission?> {
            return arrayOfNulls(size)
        }
    }
}
