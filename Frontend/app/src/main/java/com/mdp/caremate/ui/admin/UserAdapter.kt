package com.mdp.caremate.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import com.mdp.caremate.data.model.User
import com.mdp.caremate.databinding.ItemUserBinding

class UserAdapter(
    // 1. Mengubah List menjadi ArrayList agar isi datanya bisa dimanipulasi (clear & addAll)
    private val userList: ArrayList<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // 2. Fungsi krusial untuk memperbarui data secara realtime dari Fragment
    fun updateData(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            binding.tvUserRole.text = user.role

            // 3. Perbaikan Logika Badge berdasarkan Role Pengguna
            if (user.role.equals("Caregiver", ignoreCase = true)) {
                binding.tvUserRole.setBackgroundResource(R.drawable.bg_btn_secondary)
                binding.tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_green))
            } else {
                binding.tvUserRole.setBackgroundResource(R.drawable.bg_btn_secondary)
                binding.tvUserRole.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_green))
            }

            // 4. Logika Indikator Status Akun Utama (Tetap mempertahankan Active / Suspended asli Anda)
            if (user.status) {
                binding.viewStatusIndicator.setBackgroundResource(R.drawable.bg_btn_primary) // Dot Hijau
                binding.tvStatusText.text = "Active"
                binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
            } else {
                binding.viewStatusIndicator.setBackgroundResource(R.drawable.bg_tab_inactive) // Dot Abu-abu/Merah
                binding.tvStatusText.text = "Suspended"
                binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_light))
            }

            // 5. Tambahan Baru: Jika user adalah Premium, tambahkan teks penanda di sebelahnya
            if (user.isPremium) {
                // Opsi A: Menggabungkan teks ke status utama agar berdampingan rapi tanpa merusak layout XML
                binding.tvStatusText.text = "${binding.tvStatusText.text} • Premium"

                /*
                // Opsi B: Jika Anda punya TextView khusus untuk premium di xml (misal: tvPremiumBadge),
                // Anda bisa un-comment baris di bawah ini:
                binding.tvPremiumBadge.visibility = View.VISIBLE
                binding.tvPremiumBadge.text = "Premium"
                binding.tvPremiumBadge.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))
                */
            } else {
                /*
                // Reset visibility jika memakai Opsi B agar item daur ulang tidak error
                binding.tvPremiumBadge.visibility = View.GONE
                */
            }

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