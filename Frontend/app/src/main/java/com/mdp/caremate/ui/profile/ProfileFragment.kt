package com.mdp.caremate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mdp.caremate.databinding.FragmentProfileBinding

import android.app.AlertDialog
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewModel
        val firebaseSource = FirebaseSource()
        val repository = ProfileRepositoryImpl(firebaseSource)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupObservers()
        viewModel.fetchCurrentUser()

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(com.mdp.caremate.R.id.action_dest_profile_to_dest_edit_profile)
        }
        
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(com.mdp.caremate.R.id.action_dest_profile_to_login)
        }
        
        binding.cardGabungKeluarga.setOnClickListener {
            showJoinFamilyDialog()
        }
    }
    
    private fun showJoinFamilyDialog() {
        val input = EditText(requireContext())
        input.hint = "Contoh: CM-123A"
        input.setPadding(48, 48, 48, 48)

        AlertDialog.Builder(requireContext())
            .setTitle("Gabung Keluarga")
            .setMessage("Masukkan kode keluarga dari Pengguna Utama (Pasien)")
            .setView(input)
            .setPositiveButton("Kirim") { _, _ ->
                val code = input.text.toString().trim()
                if (code.isNotEmpty()) {
                    viewModel.requestJoinFamily(code)
                } else {
                    Toast.makeText(requireContext(), "Kode tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Tampilkan loading jika perlu
        }
        
        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.userState.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvProfileName.text = user.name
                binding.tvProfileJob.text = if (user.jobTitle.isEmpty()) "Caregiver" else user.jobTitle
                binding.tvProfileAge.text = if (user.age == 0) "Umur belum diatur" else "${user.age} Years Old"
                binding.tvProfileBio.text = if (user.bio.isEmpty()) "Belum ada bio" else user.bio
                
                if (user.experience.isNotEmpty()) {
                    binding.tvProfileExperience.text = user.experience.joinToString("\n") { "• $it" }
                } else {
                    binding.tvProfileExperience.text = "Belum ada pengalaman kerja"
                }

                binding.chipGroupSkills.removeAllViews()
                if (user.skills.isNotEmpty()) {
                    for (skill in user.skills) {
                        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                            text = skill
                            setChipBackgroundColorResource(android.R.color.transparent)
                            chipBackgroundColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EAF4F0"))
                            setTextColor(android.graphics.Color.parseColor("#386458"))
                        }
                        binding.chipGroupSkills.addView(chip)
                    }
                }

                if (user.pairingCode.isNotEmpty()) {
                    binding.tvPairingCode.visibility = View.VISIBLE
                    binding.tvPairingCode.text = "Kode Anda: ${user.pairingCode}"
                } else {
                    binding.tvPairingCode.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
