package com.mdp.caremate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mdp.caremate.databinding.FragmentProfileBinding

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processAndUploadPhoto(it) }
    }

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

        binding.flAvatarContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(com.mdp.caremate.R.id.action_dest_profile_to_dest_edit_profile)
        }
        
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(com.mdp.caremate.R.id.action_dest_profile_to_login)
        }
    }

    private fun processAndUploadPhoto(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            if (originalBitmap != null) {
                val maxDim = 500
                val scale = maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                val scaledBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
                } else {
                    originalBitmap
                }
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                viewModel.updateProfilePhoto(base64String)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
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
                
                if (user.photoUrl.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(user.photoUrl, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            binding.ivProfileAvatar.setImageBitmap(bitmap)
                        } else {
                            binding.ivProfileAvatar.load(user.photoUrl)
                        }
                    } catch (e: Exception) {
                        binding.ivProfileAvatar.load(user.photoUrl)
                    }
                } else {
                    binding.ivProfileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
                }

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
