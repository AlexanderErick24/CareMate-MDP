package com.mdp.caremate.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.mdp.caremate.databinding.FragmentAdminCommunityDashboardBinding

class AdminCommunityDashboardFragment : Fragment() {

    private var _binding: FragmentAdminCommunityDashboardBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCommunityDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()

        // Ambil data dari Firebase lewat ViewModel
        viewModel.fetchDashboardStats()
    }

    private fun observeViewModel() {
        // Pantau perubahan jumlah user aktif
        viewModel.activeUsersCount.observe(viewLifecycleOwner) { count ->
            binding.tvActiveUsersCount.text = String.format("%,d", count)
        }

        // Pantau perubahan jumlah event aktif
        viewModel.activeEventsCount.observe(viewLifecycleOwner) { count ->
            binding.tvActiveEventsCount.text = count.toString()
        }

        // Pantau total revenue dari user premium
        viewModel.totalRevenue.observe(viewLifecycleOwner) { revenue ->
            binding.tvTotalRevenue.text = String.format("Rp %,d", revenue)
        }

        // Pantau jumlah user premium
        viewModel.premiumUsersCount.observe(viewLifecycleOwner) { count ->
            binding.tvPremiumUsersCount.text = "$count user premium × Rp 50.000"
        }
    }

    private fun setupClickListeners() {
        // Click listeners kamu tetap aman di sini...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}