package com.mdp.caremate.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mdp.caremate.R
import com.mdp.caremate.databinding.FragmentAdminSettingBinding

class AdminSetting : Fragment() {

    private var _binding: FragmentAdminSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase Auth session
        auth = FirebaseAuth.getInstance()

        // Ambil data lokal dari user yang sedang aktif
        fetchLoggedInUser()

        // Logic Tombol Logout untuk kembali ke Halaman Login
        binding.btnAdminLogout.setOnClickListener {
            // 1. Hapus sesi login di Firebase
            FirebaseAuth.getInstance().signOut()

            Toast.makeText(context ?: return@setOnClickListener, "Berhasil Logout", Toast.LENGTH_SHORT).show()

            // 2. Restart Activity untuk membersihkan seluruh sisa UI (termasuk Bottom Nav)
            val intent = Intent(requireContext(), com.mdp.caremate.MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun fetchLoggedInUser() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email ?: "-"

            // Ambil nama dari profil auth, jika kosong ambil potongan email sebelum '@'
            val name = if (!currentUser.displayName.isNullOrEmpty()) {
                currentUser.displayName
            } else {
                email.substringBefore("@")
            }

            // Set langsung ke komponen TextView
            binding.tvAdminName.text = name
            binding.tvAdminEmail.text = email
            binding.tvAdminRole.text = "admin"

        } else {
            Toast.makeText(requireContext(), "Sesi habis, silakan login kembali.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_dest_profile_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}