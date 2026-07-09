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
import com.mdp.caremate.ui.auth.AuthViewModelFactory

import com.mdp.caremate.ui.family.management.adapter.FamilyMemberAdapter

class FamilyManagementFragment :
    Fragment(R.layout.fragment_family_management) {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var viewModel: FamilyManagementViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val rvMembers = view.findViewById<RecyclerView>(R.id.rvFamilyMembers)

        val adapter = FamilyMemberAdapter()
        rvMembers.adapter = adapter
        rvMembers.layoutManager = LinearLayoutManager(requireContext())

        val repository = AuthRepositoryImpl(FirebaseSource())

        // Factory for THIS fragment's own ViewModel
        val familyFactory = FamilyManagementViewModelFactory(repository)
        viewModel = ViewModelProvider(this, familyFactory)[FamilyManagementViewModel::class.java]

        // AuthViewModel needs its OWN factory, not familyFactory
        val authFactory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        viewModel.loadFamilyMembers()
        viewModel.familyMembers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // Load the current user (for caregiver case) AND the linked caregiver
        // (for family case) so pairingCode is available before it's needed.
        authViewModel.getCurrentUser()
        authViewModel.getLinkedCaregiver()
    }
}