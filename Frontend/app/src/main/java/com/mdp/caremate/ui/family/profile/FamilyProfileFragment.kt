package com.mdp.caremate.ui.family.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController

import com.google.firebase.auth.FirebaseAuth

import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory
import com.mdp.caremate.ui.profile.ProfileViewModel
import com.mdp.caremate.ui.profile.ProfileViewModelFactory

import java.io.ByteArrayOutputStream

class FamilyProfileFragment : Fragment(R.layout.fragment_family_profile) {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authViewModel: AuthViewModel

    // =========================
    // PHOTO PICKER
    // =========================

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult

            try {
                val inputStream =
                    requireContext().contentResolver.openInputStream(uri)

                val bitmap =
                    BitmapFactory.decodeStream(inputStream)

                inputStream?.close()

                if (bitmap != null) {
                    // Compress and convert to base64 (same approach as DashboardFragment)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(
                        android.graphics.Bitmap.CompressFormat.JPEG,
                        75,
                        outputStream
                    )
                    val base64 = Base64.encodeToString(
                        outputStream.toByteArray(),
                        Base64.NO_WRAP
                    )

                    // Update ImageView immediately for fast feedback
                    view?.findViewById<ImageView>(R.id.ivProfilePhoto)
                        ?.setImageBitmap(bitmap)

                    // Save to Firestore via ViewModel
                    profileViewModel.uploadProfilePhoto(base64)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat foto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // FIND VIEWS
        // =========================

        val ivProfilePhoto = view.findViewById<ImageView>(R.id.ivProfilePhoto)
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvRole = view.findViewById<TextView>(R.id.tvRole)
        val tvPatient = view.findViewById<TextView>(R.id.tvPatient)
        val tvCaregiver = view.findViewById<TextView>(R.id.tvCaregiver)
        val btnEditUsername = view.findViewById<Button>(R.id.btnEditUsername)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // =========================
        // SETUP VIEWMODELS
        // =========================

        val firebaseSource = FirebaseSource()

        // ProfileViewModel: handles getCurrentUser + edit actions
        val profileRepository = ProfileRepositoryImpl(firebaseSource)
        val profileFactory = ProfileViewModelFactory(profileRepository)
        profileViewModel = ViewModelProvider(this, profileFactory)[ProfileViewModel::class.java]

        // AuthViewModel: handles getLinkedCaregiver (caregiver/patient names)
        val authRepository = AuthRepositoryImpl(firebaseSource)
        val authFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        // =========================
        // LOAD DATA
        // =========================

        profileViewModel.fetchCurrentUser()
        authViewModel.getLinkedCaregiver()

        // =========================
        // OBSERVE RESULTS
        // =========================

        profileViewModel.userState.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvName.text = user.name
                tvEmail.text = user.email
                tvRole.text = user.role

                // Load profile photo if available
                if (user.photoUrl.isNotEmpty()) {
                    try {
                        val bytes = Base64.decode(user.photoUrl, Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            ivProfilePhoto.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        authViewModel.linkedCaregiver.observe(viewLifecycleOwner) { caregiver ->
            tvCaregiver.text = caregiver.name
            tvPatient.text = caregiver.patientName
        }

        profileViewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // =========================
        // PHOTO: tap avatar to change
        // =========================

        ivProfilePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // =========================
        // EDIT USERNAME
        // =========================

        btnEditUsername.setOnClickListener {
            showEditUsernameDialog()
        }

        // =========================
        // CHANGE PASSWORD
        // =========================

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // =========================
        // LOGOUT
        // =========================

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = requireActivity().intent
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    // =========================
    // DIALOG: Edit Username
    // =========================

    private fun showEditUsernameDialog() {
        val input = EditText(requireContext())
        input.hint = "Nama baru"
        input.setText(profileViewModel.userState.value?.name ?: "")
        input.setPadding(48, 48, 48, 48)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Username")
            .setMessage("Masukkan nama baru Anda")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Nama tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    profileViewModel.updateUsername(newName)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // =========================
    // DIALOG: Change Password
    // =========================

    private fun showChangePasswordDialog() {
        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 32, 48, 0)

        val etCurrentPassword = EditText(requireContext())
        etCurrentPassword.hint = "Password saat ini"
        etCurrentPassword.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val etNewPassword = EditText(requireContext())
        etNewPassword.hint = "Password baru"
        etNewPassword.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        layout.addView(etCurrentPassword)
        layout.addView(etNewPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Password")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()

                when {
                    currentPassword.isEmpty() -> {
                        Toast.makeText(
                            requireContext(),
                            "Password saat ini tidak boleh kosong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(
                            requireContext(),
                            "Password baru minimal 6 karakter",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        profileViewModel.changePassword(currentPassword, newPassword)
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}