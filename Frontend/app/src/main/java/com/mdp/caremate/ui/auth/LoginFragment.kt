package com.mdp.caremate.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // FIND VIEW
        // =========================

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        // =========================
        // FIREBASE + VIEWMODEL
        // =========================

        val firebaseSource = FirebaseSource()
        val repository = AuthRepositoryImpl(firebaseSource)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // =========================
        // LOGIN LOGIC
        // =========================

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Email dan password tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // =========================
        // OBSERVE RESULT
        // =========================

        viewModel.loginState.observe(viewLifecycleOwner) { result ->

            result.onSuccess { role ->

                val currentUid = FirebaseAuth.getInstance().currentUser?.uid

                if (currentUid != null && role != "") {

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUid)
                        .get()
                        .addOnSuccessListener { document ->

                            if (document != null && document.exists()) {

                                val isAccountActive = document.getBoolean("status") ?: true

                                if (!isAccountActive) {

                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(
                                        requireContext(),
                                        "Akun Anda ditangguhkan oleh Admin. Silakan email ke admin@caremate.com.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                } else if (role == "family") {

                                    val caregiverUid = document.getString("caregiverUid") ?: ""

                                    if (caregiverUid.isEmpty()) {

                                        // caregiverUid kosong -> tampilkan dialog reconnect
                                        // Prefill name + email dari Firestore, user hanya isi pairing code
                                        val userName = document.getString("name") ?: ""
                                        val userEmail = document.getString("email") ?: ""

                                        showReconnectDialog(
                                            userName = userName,
                                            userEmail = userEmail
                                        )

                                    } else {
                                        proceedToDashboard(role)
                                    }

                                } else {
                                    proceedToDashboard(role)
                                }

                            } else {
                                proceedToDashboard(role)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Gagal memverifikasi status akun.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {
                    proceedToDashboard(role)
                }
            }

            result.onFailure { error ->

                val message = when {

                    error.message?.contains("credential") == true ||
                            error.message?.contains("malformed") == true ||
                            error.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                        "Email atau password salah. Silakan coba lagi."

                    error.message?.contains("no user record") == true ||
                            error.message?.contains("user-not-found") == true ->
                        "Akun dengan email ini tidak ditemukan."

                    error.message?.contains("too-many-requests") == true ->
                        "Terlalu banyak percobaan login. Coba lagi nanti."

                    error.message?.contains("network") == true ||
                            error.message?.contains("Network") == true ->
                        "Tidak ada koneksi internet. Periksa jaringan kamu."

                    else -> "Login gagal: ${error.message}"
                }

                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe reconnect result dari ViewModel
        viewModel.familyQuitApproved.observe(viewLifecycleOwner) { approved ->
            if (approved == true) {
                viewModel.consumeFamilyQuitApproved()
                // Reconnect berhasil -> masuk family dashboard
                proceedToDashboard("family")
            }
        }

        viewModel.quitToastMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =========================
    // DIALOG RECONNECT
    // Muncul ketika Family login tapi caregiverUid kosong.
    // Name dan email sudah diisi otomatis dari Firestore (read-only).
    // User hanya perlu isi pairing code baru.
    // TIDAK membuat akun baru — hanya update caregiverUid di akun yang sudah ada.
    // =========================

    private fun showReconnectDialog(userName: String, userEmail: String) {

        // Inflate layout dialog manual supaya bisa custom tampilan
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reconnect_family, null)

        val etName = dialogView.findViewById<EditText>(R.id.etReconnectName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etReconnectEmail)
        val etPairingCode = dialogView.findViewById<EditText>(R.id.etReconnectPairingCode)

        // Prefill nama dan email dari Firestore, lock supaya tidak bisa diedit
        etName.setText(userName)
        etName.isEnabled = false

        etEmail.setText(userEmail)
        etEmail.isEnabled = false

        // Pairing code kosong, user isi sendiri
        etPairingCode.setText("")
        etPairingCode.isEnabled = true

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Hubungkan ke Caregiver")
            .setMessage("Akun kamu belum terhubung ke caregiver. Masukkan pairing code caregiver baru.")
            .setView(dialogView)
            .setCancelable(false) // Wajib isi, tidak bisa di-dismiss sembarangan
            .setPositiveButton("Connect", null) // null dulu supaya kita bisa validasi sebelum dismiss
            .setNegativeButton("Logout") { _, _ ->
                // Kalau tidak mau reconnect, logout saja
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(
                    requireContext(),
                    "Silakan login kembali setelah mendapat pairing code.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .create()

        dialog.show()

        // Override positiveButton supaya tidak auto-dismiss kalau pairing code kosong
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            val pairingCode = etPairingCode.text.toString().trim()

            if (pairingCode.isEmpty()) {
                etPairingCode.error = "Pairing code tidak boleh kosong"
                return@setOnClickListener
            }

            // Disable tombol supaya tidak double-tap
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Menghubungkan..."

            // Panggil reconnect -> update caregiverUid di Firestore tanpa buat akun baru
            viewModel.reconnectFamilyToNewCaregiver(pairingCode)

            // Dismiss dialog setelah request dikirim
            // Hasilnya diobserve di viewModel.familyQuitApproved dan viewModel.quitToastMessage
            dialog.dismiss()
        }
    }

    private fun proceedToDashboard(role: String) {

        when (role) {
            "caregiver" -> {
                findNavController().navigate(R.id.action_login_to_dashboard)
            }
            "family" -> {
                findNavController().navigate(R.id.familyContainerFragment)
            }
            "" -> {
                findNavController().navigate(R.id.adminDashboard)
            }
        }
    }
}