package com.mdp.caremate.ui.family.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View

import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory
import com.mdp.caremate.ui.family.management.adapter.FamilyMemberAdapter
import androidx.recyclerview.widget.LinearLayoutManager

import com.mdp.caremate.ui.family.dashboard.ActivityFeedAdapter

import android.content.Intent
import android.net.Uri
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.mdp.caremate.ui.dashboard.DashboardViewModel
import androidx.fragment.app.viewModels
import com.mdp.caremate.ui.premium.PremiumViewModel
import com.mdp.caremate.ui.premium.PremiumViewModelFactory

class FamilyDashboardFragment : Fragment(R.layout.fragment_family_dashboard) {
    private val premiumViewModel by viewModels<PremiumViewModel> { PremiumViewModelFactory }
    private lateinit var authViewModel: AuthViewModel
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        val tvGreeting =
            view.findViewById<TextView>(
                R.id.tvGreeting
            )

        val tvPatientName =
            view.findViewById<TextView>(
                R.id.tvPatientName
            )

        val tvCaregiverName =
            view.findViewById<TextView>(
                R.id.tvCaregiverName
            )

        val tvCaregiverMood = view.findViewById<TextView>(R.id.tvCaregiverMood)

        val progressDaily =
            view.findViewById<LinearProgressIndicator>(
                R.id.progressDaily
            )

        val tvProgressPercent =
            view.findViewById<TextView>(
                R.id.tvProgressPercent
            )

        val tvProgressDescription =
            view.findViewById<TextView>(
                R.id.tvProgressDescription
            )

        val btnCall112 =
            view.findViewById<ImageButton>(
                R.id.btnCall112
            )

        val btnChat =
            view.findViewById<ImageButton>(
                R.id.btnChat
            )

        val rvActivityFeed =
            view.findViewById<RecyclerView>(
                R.id.rvActivityFeed
            )

        val cardAiAlert = view.findViewById<MaterialCardView>(R.id.cardAiAlert)

        // =========================
        // NEW: Caregiver Card
        // =========================

        val cardCaregiver = view.findViewById<MaterialCardView>(R.id.cardCaregiver)

        val repository =
            AuthRepositoryImpl(
                FirebaseSource()
            )

        val factory =
            AuthViewModelFactory(
                repository
            )

        val activityAdapter =
            ActivityFeedAdapter()

        rvActivityFeed.adapter =
            activityAdapter

        rvActivityFeed.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        authViewModel =
            ViewModelProvider(
                this,
                factory
            )[AuthViewModel::class.java]

        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        authViewModel.getCurrentUser()
        authViewModel.getLinkedCaregiver()

        authViewModel.currentUser.observe(
            viewLifecycleOwner
        ) { user ->

            tvGreeting.text =
                "Hello, ${user.name} 👋"
        }

        authViewModel.linkedCaregiver.observe(
            viewLifecycleOwner
        ) { caregiver ->

            tvCaregiverName.text =
                caregiver.name

            tvPatientName.text =
                caregiver.patientName

            // Ambil data jurnal caregiver untuk melihat mood status
            premiumViewModel.initHistory(caregiver.uid)

            // =========================
            // NEW: Open caregiver profile on card tap.
            // The caregiver uid is now available, so we set the
            // click listener here where we are guaranteed to have it.
            // =========================

            cardCaregiver.setOnClickListener {
                val action = FamilyDashboardFragmentDirections
                    .actionFamilyDashboardToCaregiverDetail(
                        caregiverUid = caregiver.uid
                    )
                findNavController().navigate(action)
            }
        }

        premiumViewModel.journals.observe(viewLifecycleOwner) { journals ->
            val latestJournal = journals.maxByOrNull { it.timestamp }
            if (latestJournal != null) {
                val score = latestJournal.moodScore
                val moodText = when {
                    score <= 3 -> "Mood Status: Lelah/Burnout ($score/10) 🔴"
                    score <= 6 -> "Mood Status: Campur Aduk ($score/10) 🟡"
                    else -> "Mood Status: Bahagia/Semangat ($score/10) 🟢"
                }
                tvCaregiverMood.text = moodText

                // Ubah warna text sesuai kondisi
                if (score <= 3) {
                    tvCaregiverMood.setTextColor(android.graphics.Color.parseColor("#D32F2F")) // Merah
                } else if (score <= 6) {
                    tvCaregiverMood.setTextColor(android.graphics.Color.parseColor("#F57C00")) // Orange
                } else {
                    tvCaregiverMood.setTextColor(android.graphics.Color.parseColor("#386458")) // Hijau
                }
            } else {
                tvCaregiverMood.text = "Mood Status: Belum ada data"
                tvCaregiverMood.setTextColor(android.graphics.Color.GRAY)
            }
        }

        dashboardViewModel
            .todaysMedications
            .observe(
                viewLifecycleOwner
            ) { medications ->

                val total =
                    medications.size

                val taken =
                    medications.count {

                        it.isTakenToday
                    }

                val percentage =

                    if(total == 0)
                        0
                    else
                        (taken * 100) / total

                progressDaily.progress =
                    percentage

                tvProgressPercent.text =
                    "$percentage%"

                tvProgressDescription.text =
                    "$taken of $total medications completed today"
            }

        dashboardViewModel
            .historyList
            .observe(
                viewLifecycleOwner
            ) { historyList ->

                activityAdapter.submitList(historyList)
            }

        btnCall112.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:112")
            )
            startActivity(intent)
        }

        btnChat.setOnClickListener {
            findNavController().navigate(
                R.id.action_family_dashboard_to_chat
            )
        }

        cardAiAlert.setOnClickListener {
            findNavController().navigate(
                R.id.action_family_dashboard_to_ai_alert_history
            )
        }
    }
}