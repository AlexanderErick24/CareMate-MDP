package com.mdp.caremate.ui.family.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import com.mdp.caremate.R
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.family.dashboard.CaregiverDetailViewModel
import com.mdp.caremate.ui.family.dashboard.CaregiverDetailViewModelFactory

class CaregiverDetailFragment : Fragment(R.layout.fragment_caregiver_detail) {

    // Safe Args: receives caregiverUid from navigation
    private val args: CaregiverDetailFragmentArgs by navArgs()

    private lateinit var viewModel: CaregiverDetailViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // FIND VIEWS
        // =========================

        val tvName = view.findViewById<TextView>(R.id.tvCaregiverName)
        val tvJobTitle = view.findViewById<TextView>(R.id.tvCaregiverJobTitle)
        val tvAge = view.findViewById<TextView>(R.id.tvCaregiverAge)
        val tvEmail = view.findViewById<TextView>(R.id.tvCaregiverEmail)
        val tvPatientName = view.findViewById<TextView>(R.id.tvCaregiverPatientName)
        val tvBio = view.findViewById<TextView>(R.id.tvCaregiverBio)
        val tvExperience = view.findViewById<TextView>(R.id.tvCaregiverExperience)
        val tvSkills = view.findViewById<TextView>(R.id.tvCaregiverSkills)

        // =========================
        // SETUP VIEWMODEL
        // =========================

        val firebaseSource = FirebaseSource()
        val factory = CaregiverDetailViewModelFactory(firebaseSource)
        viewModel = ViewModelProvider(this, factory)[CaregiverDetailViewModel::class.java]

        // =========================
        // LOAD DATA
        // =========================

        viewModel.loadCaregiver(args.caregiverUid)

        // =========================
        // OBSERVE RESULT
        // =========================

        viewModel.caregiver.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvName.text = user.name
                tvJobTitle.text = if (user.jobTitle.isEmpty()) "Caregiver" else user.jobTitle
                tvAge.text = if (user.age == 0) "" else "${user.age} Years Old"
                tvEmail.text = user.email
                tvPatientName.text = user.patientName
                tvBio.text = if (user.bio.isEmpty()) "Belum ada bio" else user.bio

                tvExperience.text = if (user.experience.isNotEmpty()) {
                    user.experience.joinToString("\n") { "• $it" }
                } else {
                    "Belum ada pengalaman kerja"
                }

                tvSkills.text = if (user.skills.isNotEmpty()) {
                    user.skills.joinToString(", ")
                } else {
                    "Belum ada skill"
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}