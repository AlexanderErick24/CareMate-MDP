package com.mdp.caremate.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mdp.caremate.databinding.FragmentAdminCommunityDashboardBinding

class AdminCommunityDashboardFragment : Fragment() {

    // View Binding property backing field (valid between onCreateView and onDestroyView)
    private var _binding: FragmentAdminCommunityDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using View Binding
        _binding = FragmentAdminCommunityDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupClickListeners() {
        // Access views directly via the binding object safely
        binding.cardGrowth.setOnClickListener {
            Toast.makeText(context, "Growth Trend Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.cardBookings.setOnClickListener {
            Toast.makeText(context, "Active Bookings Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.cardRevenue.setOnClickListener {
            Toast.makeText(context, "Revenue Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.cardInsight.setOnClickListener {
            Toast.makeText(context, "Insight Action Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.ivNotification.setOnClickListener {
            Toast.makeText(context, "Notifications Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Replace these IDs with your actual IDs inside res/menu/menu_bottom_nav.xml
                // R.id.menu_home -> { true }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the reference to avoid memory leaks
        _binding = null
    }
}