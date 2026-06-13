package com.mdp.caremate.ui.family.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory


class FamilyProfileFragment : Fragment(R.layout.fragment_family_profile) {
    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val tvRole = view.findViewById<TextView>(R.id.tvRole)
        val tvPatient = view.findViewById<TextView>(R.id.tvPatient)
        val tvCaregiver = view.findViewById<TextView>(R.id.tvCaregiver)

        // =========================
        // FIREBASE + VIEWMODEL
        // =========================

        val firebaseSource = FirebaseSource()
        val repository = AuthRepositoryImpl(firebaseSource)
        val factory = AuthViewModelFactory(repository)

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[AuthViewModel::class.java]

        viewModel.getCurrentUser()
        viewModel.getLinkedCaregiver()

        // =========================
        // OBSERVE RESULT
        // =========================

        viewModel.currentUser.observe(
            viewLifecycleOwner
        ) { user ->

            tvName.text = user.name
            tvRole.text = user.role
            tvEmail.text = user.email
        }

        viewModel.linkedCaregiver.observe(
            viewLifecycleOwner
        ) { caregiver ->

            tvCaregiver.text =
                caregiver.name

            tvPatient.text =
                caregiver.patientName
        }

        // =========================
        // btn Logout
        // =========================

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance()
                .signOut()

            requireActivity()
                .findNavController(
                    R.id.nav_host_fragment
                )
                .navigate(
                    R.id.action_family_container_to_login
                )
        }


    }
}