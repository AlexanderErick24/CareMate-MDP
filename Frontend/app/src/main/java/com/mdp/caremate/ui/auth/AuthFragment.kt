package com.mdp.caremate.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        // =========================
        // FIND VIEW
        // =========================

        val etName =
            view.findViewById<EditText>(R.id.etName)

        val etEmail =
            view.findViewById<EditText>(R.id.etEmail)

        val etPassword =
            view.findViewById<EditText>(R.id.etPassword)

        val btnRegister =
            view.findViewById<Button>(R.id.btnRegister)

        val radioGroup =
            view.findViewById<RadioGroup>(R.id.radioGroupRole)

        val etPairingCode =
            view.findViewById<EditText>(R.id.etPairingCode)

        val tvPairingLabel =
            view.findViewById<TextView>(R.id.tvPairingLabel)

        val btnLogin =
            view.findViewById<Button>(R.id.btnLogin)

        // =========================
        // PAIRING VISIBILITY LOGIC
        // =========================

        radioGroup.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.rbFamily) {

                etPairingCode.visibility = View.VISIBLE
                tvPairingLabel.visibility = View.VISIBLE

            } else {

                etPairingCode.visibility = View.GONE
                tvPairingLabel.visibility = View.GONE
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
        // REGISTER BUTTON
        // =========================

        btnRegister.setOnClickListener {

            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            viewModel.registerCaregiver(
                name,
                email,
                password
            )
        }

        btnLogin.setOnClickListener {

            val email =
                etEmail.text.toString()

            val password =
                etPassword.text.toString()

            viewModel.login(
                email,
                password
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
                    "Register berhasil",
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

        viewModel.loginState.observe(
            viewLifecycleOwner
        ) { result ->

            result.onSuccess { role ->

                Toast.makeText(
                    requireContext(),
                    "Login sebagai $role",
                    Toast.LENGTH_SHORT
                ).show()

                when (role) {

                    "caregiver" -> {
                        findNavController().navigate(
                            R.id.action_dest_auth_to_dest_dashboard
                        )
                    }

                    "family" -> {

                        // TODO:
                        // navigate family dashboard
                    }

                    "admin" -> {

                        // TODO:
                        // navigate admin dashboard
                    }
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
}