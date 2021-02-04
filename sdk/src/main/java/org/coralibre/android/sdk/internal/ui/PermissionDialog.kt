package org.coralibre.android.sdk.internal.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.coralibre.android.sdk.R
import org.coralibre.android.sdk.databinding.PermissionDialogBinding

internal class PermissionDialog : DialogFragment() {
    private lateinit var permissions: List<Permission>
    private lateinit var binding: PermissionDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        val permissionsArray = requireArguments()
            .getParcelableArray(PERMISSIONS_ARG) as? Array<Permission>
            ?: throw IllegalArgumentException("$PERMISSIONS_ARG argument missing")
        permissions = permissionsArray.toList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PermissionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireDialog().apply {
            setTitle(R.string.permission_dialog_title)
        }

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = PermissionListAdapter(permissions, this::requestPermission)
    }

    private fun requestPermission(permission: Permission) {
        requestPermissions(arrayOf(permission.permission), 42)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // TODO: handle denial
        binding.list.adapter?.notifyDataSetChanged()
    }

    companion object {
        const val PERMISSIONS_ARG = "permissions"
        const val TAG = "PermissionDialog"

        private operator fun invoke(permissions: Array<Permission>): PermissionDialog {
            return PermissionDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArray(PERMISSIONS_ARG, permissions)
                }
            }
        }

        private fun getMissingPermissions(context: Context): List<Permission> {
            return Permission.values().filter { !it.isGranted(context) }
        }

        @JvmStatic
        fun hasAllPermissions(context: Context): Boolean {
            return getMissingPermissions(context).isEmpty()
        }

        @JvmStatic
        fun showPermissionsDialog(
            fragmentManager: FragmentManager,
        ) {
            PermissionDialog(Permission.values())
                .show(fragmentManager, TAG)
        }
    }
}
