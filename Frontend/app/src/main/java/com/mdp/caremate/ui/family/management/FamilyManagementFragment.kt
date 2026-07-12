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
import com.google.firebase.auth.FirebaseAuth

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
        authViewModel.loadCaregiverQuitRequestForFamily()
        authViewModel.loadFamilyQuitRequestForSelf()

        // =========================
        // OBSERVE DATA
        // =========================

        viewModel.familyMembers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // Show or hide caregiver quit request card
        authViewModel.caregiverQuitRequestForFamily.observe(viewLifecycleOwner) { request ->
            cardCaregiverQuitRequest.visibility =
                if (request != null) View.VISIBLE else View.GONE
        }

        // Quit status label per family member row
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

        // General toast dari quit actions
        authViewModel.quitToastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Family quit approved → tampilkan reconnect dialog
        authViewModel.familyQuitApproved.observe(viewLifecycleOwner) { approved ->
            if (approved) {
                authViewModel.consumeFamilyQuitApproved()
                showReconnectDialog()
            }
        }

        // =========================
        // CAREGIVER QUIT APPROVED OLEH FAMILY
        // Setelah family approve request quit dari caregiver:
        // - caregiverUid family ini sudah di-clear di Firestore
        // - Tampilkan toast penjelasan
        // - Logout supaya family tidak stuck di dashboard tanpa caregiver
        // =========================

        authViewModel.caregiverQuitApproved.observe(viewLifecycleOwner) { approved ->
            if (approved == true) {
                authViewModel.consumeCaregiverQuitApproved()
                showCaregiverQuitApprovedAndLogout()
            }
        }

        // =========================
        // CAREGIVER QUIT APPROVAL BUTTONS
        // =========================

        btnApproveCaregiverQuit.setOnClickListener {
            val request =
                authViewModel.caregiverQuitRequestForFamily.value ?: return@setOnClickListener
            showApproveCaregiverQuitDialog(request.requestId)
        }

        btnRejectCaregiverQuit.setOnClickListener {
            val request =
                authViewModel.caregiverQuitRequestForFamily.value ?: return@setOnClickListener
            showRejectCaregiverQuitDialog(request.requestId)
        }
    }

    // =========================
    // TOAST + LOGOUT setelah approve caregiver quit
    // =========================

    private fun showCaregiverQuitApprovedAndLogout() {

        Toast.makeText(
            requireContext(),
            "Kamu telah menyetujui permintaan caregiver untuk berhenti. " +
                    "Silakan login kembali dan hubungkan ke caregiver baru.",
            Toast.LENGTH_LONG
        ).show()

        // Delay sedikit supaya toast sempat terbaca sebelum logout
        view?.postDelayed({
            FirebaseAuth.getInstance().signOut()
            val intent = requireActivity().intent
            intent.addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivity(intent)
            requireActivity().finish()
        }, 2500)
    }

    // =========================
    // QUIT FAMILY CLICK HANDLER
    // =========================

    private fun handleQuitFamilyClick(member: FamilyMember) {

        val currentUser = authViewModel.currentUser.value

        if (currentUser?.uid == member.uid) {

            val existingRequest = authViewModel.familyQuitRequestForSelf.value

            if (existingRequest?.status == "rejected") {
                authViewModel.dismissFamilyQuitRequest(existingRequest.requestId)
                return
            }

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
    }

    // =========================
    // DIALOGS
    // =========================

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
                    showReconnectDialog()
                } else {
                    authViewModel.reconnectFamilyToNewCaregiver(newCode)
                }
            }
            .show()
    }

    private fun generatePairingCode(): String {
        val number = (100..999).random()
        val letter = ('A'..'Z').random()
        return "CM-$number$letter"
    }
}