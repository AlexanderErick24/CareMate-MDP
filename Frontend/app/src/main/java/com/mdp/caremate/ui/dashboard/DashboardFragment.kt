package com.mdp.caremate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var medicationAdapter: MedicationAdapter
    private var filteredMedicationCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMedicationList()
        observeMedicationList()
        setupSearch()
    }

    private fun setupMedicationList() {
        medicationAdapter = MedicationAdapter(
            onMedicationChecked = { medication, isTakenToday ->
                viewModel.updateMedicationTakenStatus(medication, isTakenToday)
            },
            onFilteredCountChanged = { filteredCount ->
                filteredMedicationCount = filteredCount
                updateEmptyState()
            }
        )
        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicationAdapter
        }
    }

    private fun observeMedicationList() {
        viewModel.todaysMedications.observe(viewLifecycleOwner) { medications ->
            medicationAdapter.setMedications(medications)
            renderSummary(medications)
            binding.rvMedications.isVisible = medications.isNotEmpty()
            updateEmptyState()
        }
    }

    private fun setupSearch() {
        binding.etDashboardSearch.doOnTextChanged { text, _, _, _ ->
            medicationAdapter.filter(text?.toString().orEmpty())
        }
    }

    private fun renderSummary(medications: List<Medication>) {
        val totalCount = medications.size
        val takenCount = medications.count { it.isTakenToday }
        val remainingCount = totalCount - takenCount

        binding.tvTotalCount.text = getString(R.string.dashboard_total_format, totalCount)
        binding.tvTakenCount.text = getString(R.string.dashboard_taken_format, takenCount)
        binding.tvSectionTitle.text = getString(R.string.dashboard_section_title)
        binding.tvDashboardSubtitle.text = getString(R.string.dashboard_subtitle)
        binding.cardTaken.alpha = if (remainingCount == 0) 1f else 0.98f
    }

    private fun updateEmptyState() {
        val hasAnyMedications = viewModel.todaysMedications.value.orEmpty().isNotEmpty()
        val noFilteredResult = hasAnyMedications && filteredMedicationCount == 0
        val noData = !hasAnyMedications

        binding.tvEmptyState.isVisible = noData || noFilteredResult
        binding.tvEmptyState.text = if (noData) {
            getString(R.string.dashboard_empty_state)
        } else {
            getString(R.string.dashboard_search_empty_state)
        }
    }

    override fun onDestroyView() {
        binding.rvMedications.adapter = null
        _binding = null
        super.onDestroyView()
    }
}