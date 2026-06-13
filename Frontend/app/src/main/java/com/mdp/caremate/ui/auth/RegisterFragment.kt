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

            result.onSuccess { message ->

                Toast.makeText(
                    requireContext(),
                    "Registrasi berhasil! Your pairing code" + message,
                    Toast.LENGTH_SHORT
                ).show()
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
}