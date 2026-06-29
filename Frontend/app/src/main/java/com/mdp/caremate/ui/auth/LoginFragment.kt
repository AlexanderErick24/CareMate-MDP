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
                            R.id.action_login_to_dashboard
                        )
                    }

                    "family" -> {

                        findNavController().navigate(
                            R.id.familyContainerFragment)
                    }

                    "admin" -> {

                        findNavController().navigate(
                            R.id.adminCommunityDashboardFragment
                        )
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