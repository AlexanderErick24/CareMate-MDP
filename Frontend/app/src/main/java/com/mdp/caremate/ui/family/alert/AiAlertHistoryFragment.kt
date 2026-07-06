package com.mdp.caremate.ui.family.alert

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.mdp.caremate.R

class AiAlertHistoryFragment : Fragment(R.layout.fragment_ai_alert_history) {

    private val viewModel: AiAlertViewModel by viewModels {
        com.mdp.caremate.ui.premium.PremiumViewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        val rvAlertHistory = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvAlertHistory)

        // Setup Adapter
        val adapter = AiAlertHistoryAdapter()
        rvAlertHistory.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        rvAlertHistory.adapter = adapter
        
        // Observe Data
        viewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            // Map dari Entity Model ke UI Item
            val uiItems = alerts.map {
                AiAlertHistoryItem(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    severity = it.severity,
                    time = android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", java.util.Date(it.timestamp)).toString(),
                    imageUrl = it.imageUrl
                )
            }
            adapter.submitList(uiItems)
        }

        // Fetch data when fragment is created
        viewModel.fetchAlerts()
    }
}
