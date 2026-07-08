package com.mdp.caremate.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

        val repository =
            AuthRepositoryImpl(firebaseSource)

        val factory =
            AuthViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[AuthViewModel::class.java]

        // =========================
        // LOGIN LOGIC
        // =========================

        btnLogin.setOnClickListener {

            val email =
                etEmail.text.toString().trim()

            val password =
                etPassword.text.toString().trim()

            viewModel.login(
                email,
                password
            )
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_login_to_register
            )
        }

        // =========================
        // OBSERVE RESULT
        // =========================

//        viewModel.loginState.observe(
//            viewLifecycleOwner
//        ) { result ->
//
//            result.onSuccess { role ->
//
//                Toast.makeText(
//                    requireContext(),
//                    "Login sebagai $role",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                when (role) {
//
//                    "caregiver" -> {
//                        findNavController().navigate(
//                            R.id.action_login_to_dashboard
//                        )
//                    }
//
//                    "family" -> {
//
//                        findNavController().navigate(
//                            R.id.familyContainerFragment)
//                    }
//
//                    "" -> {
//
//                        findNavController().navigate(
//                            R.id.adminDashboard
//                        )
//                    }
//                }
//            }
//
//            result.onFailure {
//
//                Toast.makeText(
//                    requireContext(),
//                    it.message,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }

        // cornel
        // untuk fitur admin kalau akunnya di setting inactive
        // akun tidak bisa login

        viewModel.loginState.observe(
            viewLifecycleOwner
        ) { result ->

            result.onSuccess { role ->
                // 1. Ambil UID pengguna yang baru saja sukses login dari FirebaseAuth
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

                if (currentUid != null && role != "") { // Admin ("") tidak perlu dicek statusnya jika mau dibedakan

                    // 2. Lakukan pengecekan field 'status' ke Firestore secara real-time sebelum pindah halaman
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                // Ambil field status (default true jika field tidak ditemukan)
                                val isAccountActive = document.getBoolean("status") ?: true

                                if (!isAccountActive) {
                                    // JIKA INACTIVE -> Paksa Logout dan tampilkan pesan pemblokiran
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(
                                        requireContext(),
                                        "Akun Anda ditangguhkan oleh Admin. Silakan hubungi dukungan.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    // JIKA ACTIVE -> Izinkan masuk ke halaman utama sesuai role
                                    proceedToDashboard(role)
                                }
                            } else {
                                proceedToDashboard(role)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Gagal memverifikasi status akun.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Jika yang login adalah Admin (role == ""), langsung izinkan masuk tanpa cek status
                    proceedToDashboard(role)
                }
            }

            result.onFailure {
                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
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
            "" -> { // Role Admin
                findNavController().navigate(R.id.adminDashboard)
            }
        }
    }


}