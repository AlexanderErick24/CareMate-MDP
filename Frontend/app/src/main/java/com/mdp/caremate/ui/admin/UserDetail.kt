package com.mdp.caremate.ui.admin

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.R
import com.mdp.caremate.data.model.User
import com.mdp.caremate.databinding.FragmentUserDetailBinding

class UserDetail : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    private var user: User? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ambil data Parcelable secara aman (Mendukung Android 13 / Tiramisu ke atas)
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("ARG_USER", User::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("ARG_USER")
        }

        // 2. Tampilkan data ke komponen UI jika data tidak null
        user?.let { currentUser ->
            displayUserData(currentUser)
            setupStatusSwitch(currentUser)
        }

        // 3. Setup klik tombol kembali
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun displayUserData(user: User) {
        // Set Informasi dasar
        binding.tvDetailName.text = user.name
        binding.tvDetailEmail.text = user.email
        binding.tvDetailRole.text = user.role

        // Atur warna badge / background role secara dinamis
        if (user.role.equals("Caregiver", ignoreCase = true)) {
            binding.tvDetailRole.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            binding.tvDetailRole.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            // Tampilkan section CV Caregiver, Sembunyikan section Family
            binding.cardCaregiverCvSection.visibility = View.VISIBLE
            binding.cardFamilySection.visibility = View.GONE

            // Bind data CV Caregiver
            binding.tvJobTitle.text = "Job Title: ${user.jobTitle.ifEmpty { "-" }}"
            binding.tvAge.text = "Umur: ${user.age} Tahun"
            binding.tvBio.text = "Bio: ${user.bio.ifEmpty { "-" }}"

            // Gabungkan List<String> menjadi teks berbaris/berkoma
            binding.tvExperienceList.text = if (user.experience.isNotEmpty()) {
                user.experience.joinToString("\n") { "- $it" }
            } else {
                "-"
            }
            binding.tvSkillsList.text = if (user.skills.isNotEmpty()) {
                user.skills.joinToString(", ")
            } else {
                "-"
            }

        } else {
            binding.tvDetailRole.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            binding.tvDetailRole.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

            // Tampilkan section Family, Sembunyikan section CV Caregiver
            binding.cardCaregiverCvSection.visibility = View.GONE
            binding.cardFamilySection.visibility = View.VISIBLE

            // Bind data Hubungan Keluarga
            binding.tvPairingCode.text = "Pairing Code: ${user.pairingCode.ifEmpty { "-" }}"
            binding.tvPatientName.text = "Nama Pasien: ${user.patientName.ifEmpty { "-" }}"
            binding.tvConnectedPatientUid.text = "Connected Patient UID: ${user.connectedPatientUid.ifEmpty { "-" }}"
        }

        // Set kondisi awal status switch (Aktif/Blokir)
        binding.switchStatus.isChecked = user.status
    }

    private fun setupStatusSwitch(user: User) {
        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            // Update status secara real-time ke Firebase Firestore berdasarkan UID user
            db.collection("users").document(user.uid)
                .update("status", isChecked)
                .addOnSuccessListener {
                    val message = if (isChecked) "Akun berhasil diaktifkan" else "Akun berhasil dinonaktifkan/ditangguhkan"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Kembalikan posisi switch jika gagal update ke database
                    binding.switchStatus.isChecked = !isChecked
                    Toast.makeText(requireContext(), "Gagal mengubah status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}