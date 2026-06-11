package com.mdp.caremate.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.databinding.FragmentDashboardBinding
import com.mdp.caremate.ui.medicationform.MedFormFragment

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
        setupSwipeToDelete()
        observeMedicationList()
        setupSearch()
        
        binding.btnHistory.setOnClickListener {
            HistoryBottomSheetFragment().show(childFragmentManager, HistoryBottomSheetFragment.TAG)
        }
    }

    private fun setupMedicationList() {
        medicationAdapter = MedicationAdapter(
            onMedicationChecked = { medication, isTakenToday ->
                viewModel.updateMedicationTakenStatus(medication, isTakenToday)
            },
            onMedicationEdit = { medication ->
                openMedicationForm(medication.id)
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

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) return

                    val medication = medicationAdapter.currentList.getOrNull(position)
                    if (medication != null) {
                        confirmDeleteMedication(medication, position)
                    } else {
                        medicationAdapter.notifyItemChanged(position)
                    }
                }
            }
        )
        itemTouchHelper.attachToRecyclerView(binding.rvMedications)
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
        binding.fabAddMedication.setOnClickListener {
            findNavController().navigate(R.id.action_dest_dashboard_to_dest_med_form)
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

    private fun openMedicationForm(medicationId: Long) {
        val args = Bundle().apply {
            putLong(MedFormFragment.ARG_MEDICATION_ID, medicationId)
        }
        findNavController().navigate(R.id.dest_med_form, args)
    }

    private fun confirmDeleteMedication(medication: Medication, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.confirm_delete_positive) { _, _ ->
                viewModel.deleteMedication(medication)
            }
            .setNegativeButton(R.string.confirm_delete_negative) { _, _ ->
                medicationAdapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                medicationAdapter.notifyItemChanged(position)
            }
            .show()
    }

    override fun onDestroyView() {
        binding.rvMedications.adapter = null
        _binding = null
        super.onDestroyView()
    }
}