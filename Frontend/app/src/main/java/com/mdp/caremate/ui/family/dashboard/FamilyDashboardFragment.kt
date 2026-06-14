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
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.mdp.caremate.ui.dashboard.DashboardViewModel

class FamilyDashboardFragment : Fragment(R.layout.fragment_family_dashboard) {
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
            ) { history ->

                activityAdapter.submitList(
                    history.sortedByDescending {

                        it.takenAt
                    }
                )
            }

        btnCall112.setOnClickListener {

            val intent =
                Intent(
                    Intent.ACTION_DIAL
                )

            intent.data =
                Uri.parse(
                    "tel:112"
                )

            startActivity(intent)
        }

        btnChat.setOnClickListener {

            findNavController().navigate(
                R.id.action_family_dashboard_to_chat
            )
        }
    }

}