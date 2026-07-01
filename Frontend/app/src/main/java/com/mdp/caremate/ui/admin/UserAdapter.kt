package com.mdp.caremate.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import com.mdp.caremate.data.model.User
import com.mdp.caremate.databinding.ItemUserBinding // Pastikan layout item bernama item_user.xml

class UserAdapter(
    private val userList: List<User>,
    private val onItemClick: (User) -> Unit // Callback untuk menghandle klik pada item user
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            // 1. Set Data Tekstual Utama
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            binding.tvUserRole.text = user.role

            // 2. Logika Badge / Chip berdasarkan Role Pengguna
            if (user.role.equals("Caregiver", ignoreCase = true)) {
                binding.tvUserRole.setBackgroundResource(R.drawable.bg_btn_secondary) // contoh file drawable
                binding.tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_green))
            } else {
                binding.tvUserRole.setBackgroundResource(R.drawable.bg_btn_secondary)
                binding.tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_green))
            }

            // 3. Logika Indikator Status Akun (Aktif / Non-aktif)
            if (user.status) {
                binding.viewStatusIndicator.setBackgroundResource(R.drawable.bg_btn_primary) // Dot Hijau
                binding.tvStatusText.text = "Active"
                binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
            } else {
                binding.viewStatusIndicator.setBackgroundResource(R.drawable.bg_tab_inactive) // Dot Abu-abu/Merah
                binding.tvStatusText.text = "Suspended"
                binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_light))
            }

            // 4. Event Listener Klik Item
            itemView.setOnClickListener { onItemClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size
}