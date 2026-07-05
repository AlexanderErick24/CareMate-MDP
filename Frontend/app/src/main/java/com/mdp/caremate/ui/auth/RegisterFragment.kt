package com.mdp.caremate.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // FIND VIEW
        // =========================

        val etName = view.findViewById<EditText>(R.id.etName)
        val etPatientName = view.findViewById<EditText>(R.id.etPatientName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupRole)
        val etPairingCode = view.findViewById<EditText>(R.id.etPairingCode)
        val rbCaregiver = view.findViewById<RadioButton>(R.id.rbCaregiver)
        val rbFamily = view.findViewById<RadioButton>(R.id.rbFamily)
        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)

        // =========================
        // PAIRING VISIBILITY LOGIC
        // =========================

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.rbFamily) {

                etPairingCode.visibility = View.VISIBLE
                etPatientName.visibility = View.GONE

            } else {

                etPairingCode.visibility = View.GONE
                etPatientName.visibility = View.VISIBLE
            }
        }

        // =========================
        // HANDLE ADD FAMILY MEMBER
        // =========================
        val isFromFamily = arguments?.getBoolean("isFromFamily") ?: false
        val pairingCodeFromArg = arguments?.getString("pairingCode") ?: ""

        if (isFromFamily) {
            // 1. Langsung otomatis kepencet radio button Family
            rbFamily.isChecked = true

            // 2. Hilangkan radio button Caregiver (sesuai request "gausa ada")
            rbCaregiver.visibility = View.GONE

            // 3. Pastikan Pairing Code muncul & Patient Name hilang
            etPairingCode.visibility = View.VISIBLE
            etPatientName.visibility = View.GONE

            // 4. (Opsional tapi direkomendasikan)
            // Langsung isikan pairing code-nya dan kunci biar user ga salah ketik
            etPairingCode.setText(pairingCodeFromArg)
            etPairingCode.isEnabled = false
        }

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

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val patientName = etPatientName.text.toString()

            val role =
                when {
                    rbCaregiver.isChecked ->
                        "caregiver"
                    rbFamily.isChecked ->
                        "family"
                    else ->
                        ""
                }

            val pairingCode =
                if (role == "family")
                    etPairingCode.text.toString()
                else
                    ""

            viewModel.register(
                name,
                email,
                password,
                role,
                pairingCode,
                patientName
            )
        }

        tvLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_register_to_login
            )
        }

        // =========================
        // OBSERVE RESULT
        // =========================

        viewModel.registerState.observe(
            viewLifecycleOwner
        ) { result ->

            result.onSuccess {

                Toast.makeText(
                    requireContext(),
                    "Register Success",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.loginFragment
                )
            }

            result.onFailure { error ->

                when(error.message) {

                    "PREMIUM_REQUIRED" -> {

                        Toast.makeText(
                            requireContext(),
                            "Maximum free family members reached. Upgrade to Premium.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {

                        Toast.makeText(
                            requireContext(),
                            error.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    }
}