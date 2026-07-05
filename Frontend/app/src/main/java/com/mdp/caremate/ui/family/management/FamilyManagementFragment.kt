package com.mdp.caremate.ui.family.management

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController

import com.mdp.caremate.R

import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.dashboard.DashboardViewModel

import com.mdp.caremate.ui.family.management.adapter.FamilyMemberAdapter

class FamilyManagementFragment :
    Fragment(
        R.layout.fragment_family_management
    ) {
    private lateinit var authViewModel: AuthViewModel

    private lateinit var viewModel:
            FamilyManagementViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        val rvMembers =
            view.findViewById<RecyclerView>(
                R.id.rvFamilyMembers
            )

        val btnAddFamily =
            view.findViewById<Button>(
                R.id.btnAddFamily
            )

        val adapter =
            FamilyMemberAdapter()

        rvMembers.adapter =
            adapter

        rvMembers.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        val repository =
            AuthRepositoryImpl(
                FirebaseSource()
            )

        val factory =
            FamilyManagementViewModelFactory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[FamilyManagementViewModel::class.java]

        viewModel.loadFamilyMembers()

        viewModel.familyMembers.observe(
            viewLifecycleOwner
        ) {

            adapter.submitList(it)
        }

        authViewModel =
            ViewModelProvider(
                this,
                factory
            )[AuthViewModel::class.java]

        val userPairingCode = authViewModel.currentUser.value?.pairingCode ?: ""

        btnAddFamily.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Register your new member now",
                Toast.LENGTH_LONG
            ).show()
            val action =
                FamilyManagementFragmentDirections
                    .actionFamilyManagementToRegister(
                        isFromFamily = true,
                        pairingCode = userPairingCode
                    )

            findNavController().navigate(action)
        }

        viewModel.isPremiumRequired.observe(
            viewLifecycleOwner
        ) {

            if(it) {

                Toast.makeText(
                    requireContext(),
                    "Upgrade to Premium first",
                    // TODO() -> logic premium
                    Toast.LENGTH_LONG
                ).show()

            } else {

                findNavController().navigate(
                    R.id.action_family_management_to_register
                )
            }
        }
    }
}