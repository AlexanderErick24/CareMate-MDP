package com.mdp.caremate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.mdp.caremate.R
import com.mdp.caremate.data.model.QuitRequest
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authViewModel: AuthViewModel

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

        val firebaseSource = FirebaseSource()

        // =========================
        // SETUP VIEWMODELS
        // =========================

        val profileRepository = ProfileRepositoryImpl(firebaseSource)
        val profileFactory = ProfileViewModelFactory(profileRepository)
        viewModel = ViewModelProvider(this, profileFactory)[ProfileViewModel::class.java]

        val authRepository = AuthRepositoryImpl(firebaseSource)
        val authFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        // =========================
        // LOAD DATA
        // =========================

        viewModel.fetchCurrentUser()

        // Load caregiver's own quit request status (pending/rejected)
        authViewModel.loadCaregiverQuitRequest()

        // Load any family member quit request directed at the caregiver
        authViewModel.loadFamilyQuitRequestForCaregiver()

        // =========================
        // SETUP LISTENERS
        // =========================

        binding.flAvatarContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dest_profile_to_dest_edit_profile)
        }

        // QUIT PATIENT button
        binding.btnQuitPatient.setOnClickListener {
            showQuitPatientConfirmDialog()
        }

        // Dismiss the rejected quit request
        binding.btnDismissCaregiverQuit.setOnClickListener {
            val request = authViewModel.caregiverQuitRequest.value
            if (request != null) {
                authViewModel.dismissCaregiverQuitRequest(request.requestId)
            }
        }

        // Approve family quit request
        binding.btnApproveFamilyQuit.setOnClickListener {
            val request = authViewModel.familyQuitRequestForCaregiver.value
            if (request != null) {
                authViewModel.approveFamilyQuit(request.requestId)
            }
        }

        // Reject family quit request
        binding.btnRejectFamilyQuit.setOnClickListener {
            val request = authViewModel.familyQuitRequestForCaregiver.value
            if (request != null) {
                showRejectFamilyQuitDialog(request.requestId)
            }
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = requireActivity().intent
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

        setupObservers()
    }

    // =========================
    // OBSERVERS
    // =========================

    private fun setupObservers() {

        viewModel.isLoading.observe(viewLifecycleOwner) {
            // Show loading if needed
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

        // Observe caregiver's own quit request status
        authViewModel.caregiverQuitRequest.observe(viewLifecycleOwner) { request ->
            updateCaregiverQuitStatusCard(request)
        }

        // Observe family quit request directed at this caregiver
        authViewModel.familyQuitRequestForCaregiver.observe(viewLifecycleOwner) { request ->
            updateFamilyQuitRequestCard(request)
        }

        // Observe toast messages from quit actions
        authViewModel.quitToastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =========================
    // QUIT STATUS CARD LOGIC
    // =========================

    private fun updateCaregiverQuitStatusCard(request: QuitRequest?) {

        if (request == null) {
            binding.cardCaregiverQuitStatus.visibility = View.GONE
            return
        }

        binding.cardCaregiverQuitStatus.visibility = View.VISIBLE

        when (request.status) {

            "pending" -> {
                binding.tvCaregiverQuitStatusTitle.text = "Quit Request Sent"
                binding.tvCaregiverQuitStatusMessage.text =
                    "Waiting for family approval..."
                binding.btnDismissCaregiverQuit.visibility = View.GONE
            }

            "rejected" -> {
                binding.tvCaregiverQuitStatusTitle.text = "Quit Request Rejected"
                binding.tvCaregiverQuitStatusMessage.text =
                    "Reason: ${request.reason}"
                binding.btnDismissCaregiverQuit.visibility = View.VISIBLE
            }
        }
    }

    private fun updateFamilyQuitRequestCard(request: QuitRequest?) {

        if (request == null) {
            binding.cardFamilyQuitRequest.visibility = View.GONE
            return
        }

        binding.cardFamilyQuitRequest.visibility = View.VISIBLE

        // We show the familyUid as identifier since we don't have their name here.
        // In a future improvement you could load their name from Firestore.
        binding.tvFamilyQuitRequesterName.text =
            "A family member (ID: ${request.familyUid.take(8)}...) wants to disconnect from you."
    }

    // =========================
    // DIALOGS
    // =========================

    private fun showQuitPatientConfirmDialog() {

        AlertDialog.Builder(requireContext())
            .setTitle("Quit Patient")
            .setMessage(
                "Are you sure you want to quit this patient? " +
                        "A request will be sent to all linked family members. " +
                        "You will need to wait for their approval."
            )
            .setPositiveButton("Send Request") { _, _ ->
                authViewModel.sendCaregiverQuitRequest()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectFamilyQuitDialog(requestId: String) {

        val input = EditText(requireContext())
        input.hint = "Rejection reason"

        AlertDialog.Builder(requireContext())
            .setTitle("Reject Family Quit")
            .setMessage("Please provide a reason for rejection:")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a reason",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    authViewModel.rejectFamilyQuit(requestId, reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================
    // PHOTO UPLOAD
    // =========================

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}