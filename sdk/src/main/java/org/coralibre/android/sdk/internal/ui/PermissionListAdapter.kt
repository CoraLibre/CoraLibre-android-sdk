package org.coralibre.android.sdk.internal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.coralibre.android.sdk.R
import org.coralibre.android.sdk.databinding.PermissionDialogItemBinding

class PermissionListAdapter(
    private val permissions: List<Permission>,
    private val requestPermission: (Permission) -> Unit,
) : RecyclerView.Adapter<PermissionListAdapter.PermissionItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PermissionDialogItemBinding.inflate(inflater, parent, false)
        return PermissionItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PermissionItemViewHolder, position: Int) {
        holder.bind(permissions[position])
    }

    override fun getItemCount(): Int = permissions.size

    inner class PermissionItemViewHolder(
        private val binding: PermissionDialogItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private fun isGranted(permission: Permission): Boolean {
            return itemView.context.checkSelfPermission(permission)
        }

        fun bind(permission: Permission) {
            binding.permissionTitle.setText(permission.displayName)
            binding.permissionDescription.setText(permission.description)

            if (isGranted(permission)) {
                binding.grant.isEnabled = false
                binding.grant.setText(R.string.permission_granted)
            } else {
                binding.grant.setOnClickListener {
                    requestPermission(permission)
                }
            }
        }
    }
}
