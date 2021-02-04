package org.coralibre.android.sdk.internal.ui

import android.content.Context
import androidx.fragment.app.FragmentManager

object CoraPermissions {
    @JvmStatic
    fun hasAllPermissions(context: Context): Boolean {
        return PermissionDialog.hasAllPermissions(context)
    }

    @JvmStatic
    fun showPermissionDialog(fragmentManager: FragmentManager) {
        PermissionDialog.showPermissionsDialog(fragmentManager)
    }
}
