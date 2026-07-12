package com.mdp.caremate.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.mdp.caremate.R
import com.mdp.caremate.databinding.FragmentProfileBinding
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { processAndUploadPhoto(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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

        binding.flAvatarContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dest_profile_to_dest_edit_profile)
        }

        // Tombol edit nama pasien — buka dialog
        binding.ivEditPatientName.setOnClickListener {
            showEditPatientNameDialog()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = requireActivity().intent
            intent.addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupObservers() {

        viewModel.isLoading.observe(viewLifecycleOwner) {
            // Tampilkan loading jika perlu
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.userState.observe(viewLifecycleOwner) { user ->
            if (user != null) {

                binding.tvProfileName.text = user.name
                binding.tvProfileJob.text =
                    if (user.jobTitle.isEmpty()) "Caregiver" else user.jobTitle
                binding.tvProfileAge.text =
                    if (user.age == 0) "Umur belum diatur" else "${user.age} Years Old"
                binding.tvProfileBio.text =
                    if (user.bio.isEmpty()) "Belum ada bio" else user.bio

                // =====================
                // PATIENT NAME
                // Tampilkan nama pasien, atau placeholder kalau masih kosong
                // (terjadi setelah quit → pairing code baru belum ada patient)
                // =====================
                if (user.patientName.isEmpty()) {
                    binding.tvPatientName.text = "Belum diisi"
                    binding.tvPatientName.setTextColor(
                        android.graphics.Color.parseColor("#AAAAAA")
                    )
                } else {
                    binding.tvPatientName.text = user.patientName
                    binding.tvPatientName.setTextColor(
                        android.graphics.Color.parseColor("#191C1B")
                    )
                }

                // Photo
                if (user.photoUrl.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(user.photoUrl, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(
                            decodedBytes, 0, decodedBytes.size
                        )
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

                // Experience
                if (user.experience.isNotEmpty()) {
                    binding.tvProfileExperience.text =
                        user.experience.joinToString("\n") { "• $it" }
                } else {
                    binding.tvProfileExperience.text = "Belum ada pengalaman kerja"
                }

                // Skills chips
                binding.chipGroupSkills.removeAllViews()
                if (user.skills.isNotEmpty()) {
                    for (skill in user.skills) {
                        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                            text = skill
                            chipBackgroundColor =
                                android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#EAF4F0")
                                )
                            setTextColor(android.graphics.Color.parseColor("#386458"))
                        }
                        binding.chipGroupSkills.addView(chip)
                    }
                }

                // Pairing code
                if (user.pairingCode.isNotEmpty()) {
                    binding.tvPairingCode.visibility = View.VISIBLE
                    binding.tvPairingCode.text = "Kode Anda: ${user.pairingCode}"
                } else {
                    binding.tvPairingCode.visibility = View.GONE
                }
            }
        }
    }

    // =====================
    // DIALOG EDIT NAMA PASIEN
    // Prefill dengan nama pasien yang sudah ada (kalau ada).
    // Kalau masih kosong, field kosong supaya user langsung isi.
    // =====================
    private fun showEditPatientNameDialog() {

        val currentPatientName = viewModel.userState.value?.patientName ?: ""

        val input = EditText(requireContext()).apply {
            hint = "Nama pasien"
            if (currentPatientName.isNotEmpty()) {
                setText(currentPatientName)
                // Posisikan cursor di akhir teks supaya enak diedit
                setSelection(currentPatientName.length)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nama Pasien")
            .setMessage("Masukkan nama pasien yang sedang kamu rawat.")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Nama pasien tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.updatePatientName(newName)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processAndUploadPhoto(uri: Uri) {
        try {
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            if (originalBitmap != null) {
                val maxDim = 500
                val scale =
                    maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                val scaledBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (originalBitmap.width * scale).toInt(),
                        (originalBitmap.height * scale).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val base64String =
                    Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                viewModel.updateProfilePhoto(base64String)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}