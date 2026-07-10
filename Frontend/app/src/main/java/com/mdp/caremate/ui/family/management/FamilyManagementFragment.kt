package com.mdp.caremate.ui.family.management

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton

import androidx.navigation.fragment.findNavController

import com.mdp.caremate.R

import com.mdp.caremate.data.model.FamilyMember
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
        val cardCaregiverQuitRequest = view.findViewById<MaterialCardView>(R.id.cardCaregiverQuitRequest)
        val btnApproveCaregiverQuit = view.findViewById<MaterialButton>(R.id.btnApproveCaregiverQuit)
        val btnRejectCaregiverQuit = view.findViewById<MaterialButton>(R.id.btnRejectCaregiverQuit)

        val repository = AuthRepositoryImpl(FirebaseSource())

        // =========================
        // SETUP VIEWMODELS
        // =========================

        val familyFactory = FamilyManagementViewModelFactory(repository)
        viewModel = ViewModelProvider(this, familyFactory)[FamilyManagementViewModel::class.java]

        val authFactory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]

        // =========================
        // SETUP ADAPTER
        // =========================

        // The adapter receives a callback when "Quit Family" is pressed on a member row.
        // Since FamilyManagementFragment is visible to BOTH caregiver and family roles:
        // - Caregiver sees all family members with Quit buttons (to remove a member)
        // - A family member viewing their own row can press Quit to send a quit request
        // We use authViewModel.currentUser to decide which flow to trigger.
        val adapter = FamilyMemberAdapter(
            onQuitClick = { member ->
                handleQuitFamilyClick(member)
            }
        )

        rvMembers.adapter = adapter
        rvMembers.layoutManager = LinearLayoutManager(requireContext())

        // =========================
        // LOAD DATA
        // =========================

        viewModel.loadFamilyMembers()
        authViewModel.getCurrentUser()
        authViewModel.getLinkedCaregiver()

        // Load the caregiver quit request visible to this family member
        authViewModel.loadCaregiverQuitRequestForFamily()

        // Load self quit request status (for family member's own row status)
        authViewModel.loadFamilyQuitRequestForSelf()

        // =========================
        // OBSERVE DATA
        // =========================

        viewModel.familyMembers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // Show or hide the caregiver quit request card
        authViewModel.caregiverQuitRequestForFamily.observe(viewLifecycleOwner) { request ->
            if (request != null) {
                cardCaregiverQuitRequest.visibility = View.VISIBLE
            } else {
                cardCaregiverQuitRequest.visibility = View.GONE
            }
        }

        // Update quit status labels on each family member row
        authViewModel.familyQuitRequestForSelf.observe(viewLifecycleOwner) { request ->
            if (request != null) {
                val statusText = when (request.status) {
                    "pending" -> "Waiting for caregiver approval..."
                    "rejected" -> "Rejected: ${request.reason} (tap Quit again to dismiss)"
                    else -> null
                }
                if (statusText != null) {
                    adapter.setQuitStatusMap(mapOf(request.familyUid to statusText))
                } else {
                    adapter.setQuitStatusMap(emptyMap())
                }
            } else {
                adapter.setQuitStatusMap(emptyMap())
            }
        }

        // Toast messages from quit actions
        authViewModel.quitToastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // After family quit is approved, show reconnect dialog
        authViewModel.familyQuitApproved.observe(viewLifecycleOwner) { approved ->
            if (approved) {
                authViewModel.consumeFamilyQuitApproved()
                showReconnectDialog()
            }
        }

        // =========================
        // CAREGIVER QUIT APPROVAL BUTTONS
        // =========================

        btnApproveCaregiverQuit.setOnClickListener {
            val request = authViewModel.caregiverQuitRequestForFamily.value ?: return@setOnClickListener
            showApproveCaregiverQuitDialog(request.requestId)
        }

        btnRejectCaregiverQuit.setOnClickListener {
            val request = authViewModel.caregiverQuitRequestForFamily.value ?: return@setOnClickListener
            showRejectCaregiverQuitDialog(request.requestId)
        }
    }

    // =========================
    // QUIT FAMILY CLICK HANDLER
    // =========================

    private fun handleQuitFamilyClick(member: FamilyMember) {

        val currentUser = authViewModel.currentUser.value

        // If the current user is the family member themselves clicking their own row
        if (currentUser?.uid == member.uid) {

            val existingRequest = authViewModel.familyQuitRequestForSelf.value

            // If there's a rejected request, dismiss it first, then allow re-send
            if (existingRequest?.status == "rejected") {
                authViewModel.dismissFamilyQuitRequest(existingRequest.requestId)
                return
            }

            // Send a new quit request
            AlertDialog.Builder(requireContext())
                .setTitle("Quit Family")
                .setMessage(
                    "Are you sure you want to disconnect from your current caregiver? " +
                            "A request will be sent to the caregiver for approval."
                )
                .setPositiveButton("Send Request") { _, _ ->
                    authViewModel.sendFamilyQuitRequest()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        // If the current user is the caregiver viewing the family list,
        // the Quit button is not the right action for them here.
        // Family quit is initiated by the family member only.
    }

    // =========================
    // DIALOGS
    // =========================

    // Family member approves caregiver quit — needs to enter new patient name
    // (The new pairing code is generated internally in FirebaseSource)
    private fun showApproveCaregiverQuitDialog(requestId: String) {

        val input = EditText(requireContext())
        input.hint = "New patient name"

        AlertDialog.Builder(requireContext())
            .setTitle("Approve Caregiver Quit")
            .setMessage(
                "Please enter the new patient's name. " +
                        "A new pairing code will be generated for the caregiver."
            )
            .setView(input)
            .setPositiveButton("Approve") { _, _ ->
                val newPatientName = input.text.toString().trim()
                if (newPatientName.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a patient name",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Generate the new pairing code here so we can pass it in
                    val newPairingCode = generatePairingCode()
                    authViewModel.approveCaregiverQuit(
                        requestId,
                        newPatientName,
                        newPairingCode
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectCaregiverQuitDialog(requestId: String) {

        val input = EditText(requireContext())
        input.hint = "Rejection reason"

        AlertDialog.Builder(requireContext())
            .setTitle("Reject Caregiver Quit")
            .setMessage("Please provide a reason for rejection:")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a reason",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    authViewModel.rejectCaregiverQuit(requestId, reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // After family quit is approved, ask for a new pairing code to reconnect
    private fun showReconnectDialog() {

        val input = EditText(requireContext())
        input.hint = "New caregiver pairing code"

        AlertDialog.Builder(requireContext())
            .setTitle("Connect to New Caregiver")
            .setMessage(
                "Your quit request was approved. " +
                        "Please enter your new caregiver's pairing code to reconnect."
            )
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Connect") { _, _ ->
                val newCode = input.text.toString().trim()
                if (newCode.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a pairing code",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Show dialog again if empty
                    showReconnectDialog()
                } else {
                    authViewModel.reconnectFamilyToNewCaregiver(newCode)
                }
            }
            .show()
    }

    // Generates a pairing code in the same format as FirebaseSource
    // This is passed to approveCaregiverQuit so the new code is known
    private fun generatePairingCode(): String {
        val number = (100..999).random()
        val letter = ('A'..'Z').random()
        return "CM-$number$letter"
    }
}