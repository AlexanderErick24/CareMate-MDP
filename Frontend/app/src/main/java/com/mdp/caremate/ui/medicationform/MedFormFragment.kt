package com.mdp.caremate.ui.medicationform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mdp.caremate.R
import com.mdp.caremate.databinding.FragmentMedFormBinding

class MedFormFragment : Fragment() {
    private var _binding: FragmentMedFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val medicationId = arguments?.getString(ARG_MEDICATION_ID) ?: ""

        setupActions()
        observeState()
        if (medicationId.isNotEmpty()) {
            viewModel.loadMedication(medicationId)
        }
    }

    private fun setupActions() {
        binding.btnSaveMedication.setOnClickListener {
            viewModel.saveMedication(
                name = binding.etMedName.text?.toString().orEmpty(),
                dosage = binding.etMedDosage.text?.toString().orEmpty(),
                hourText = binding.etMedHour.text?.toString().orEmpty(),
                minuteText = binding.etMedMinute.text?.toString().orEmpty()
            )
        }
        binding.btnDeleteMedication.setOnClickListener {
            viewModel.deleteMedication()
        }
    }

    private fun observeState() {
        viewModel.selectedMedication.observe(viewLifecycleOwner) { medication ->
            if (medication != null) {
                binding.etMedName.setText(medication.name)
                binding.etMedDosage.setText(medication.dosage)
                binding.etMedHour.setText(medication.intakeHour.toString())
                binding.etMedMinute.setText(medication.intakeMinute.toString())
            }
        }

        viewModel.isEditing.observe(viewLifecycleOwner) { isEditing ->
            binding.tvMedFormTitle.text = getString(
                if (isEditing) R.string.med_form_title_edit else R.string.med_form_title_add
            )
            binding.btnSaveMedication.text = getString(
                if (isEditing) R.string.med_form_update else R.string.med_form_save
            )
            binding.btnDeleteMedication.isVisible = isEditing
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        viewModel.closeScreen.observe(viewLifecycleOwner) { shouldClose ->
            if (shouldClose) {
                viewModel.consumeCloseScreen()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_MEDICATION_ID = "medication_id"
    }
}
