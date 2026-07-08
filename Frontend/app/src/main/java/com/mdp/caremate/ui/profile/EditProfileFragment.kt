package com.mdp.caremate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.databinding.FragmentEditProfileBinding
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processAndUploadPhoto(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseSource = FirebaseSource()
        val repository = ProfileRepositoryImpl(firebaseSource)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupObservers()
        viewModel.fetchCurrentUser()

        binding.flEditAvatarContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etProfileName.text.toString().trim()
            val jobTitle = binding.etProfileJob.text.toString().trim()
            val age = binding.etProfileAge.text.toString().trim().toIntOrNull() ?: 0
            val bio = binding.etProfileBio.text.toString().trim()
            
            val expLines = binding.etProfileExp.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                
            val skillsList = binding.etProfileSkills.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateUserProfile(name, jobTitle, age, bio, expLines, skillsList)
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
            binding.btnSaveProfile.isEnabled = !isLoading
            binding.btnSaveProfile.text = if (isLoading) "Menyimpan..." else "Simpan Perubahan"
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.userState.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                if (user.photoUrl.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(user.photoUrl, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            binding.ivEditProfileAvatar.setImageBitmap(bitmap)
                        } else {
                            binding.ivEditProfileAvatar.load(user.photoUrl)
                        }
                    } catch (e: Exception) {
                        binding.ivEditProfileAvatar.load(user.photoUrl)
                    }
                } else {
                    binding.ivEditProfileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                if (binding.etProfileName.text.isNullOrEmpty()) {
                    binding.etProfileName.setText(user.name)
                    binding.etProfileJob.setText(user.jobTitle)
                    if (user.age > 0) binding.etProfileAge.setText(user.age.toString())
                    binding.etProfileBio.setText(user.bio)
                    binding.etProfileExp.setText(user.experience.joinToString("\n"))
                    binding.etProfileSkills.setText(user.skills.joinToString(", "))
                }
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                viewModel.resetUpdateSuccess()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
