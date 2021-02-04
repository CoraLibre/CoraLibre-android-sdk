package org.coralibre.android.sdk.internal.ui

import android.content.Context
import android.content.pm.PackageManager

fun Context.checkSelfPermission(permission: Permission): Boolean {
    return when (checkSelfPermission(permission.permission)) {
        PackageManager.PERMISSION_GRANTED -> true
        PackageManager.PERMISSION_DENIED -> false
        else -> throw IllegalStateException("Invalid return code")
    }
}
