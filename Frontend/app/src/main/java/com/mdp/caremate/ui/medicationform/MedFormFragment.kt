package com.mdp.caremate.ui.medicationform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mdp.caremate.R
import com.mdp.caremate.databinding.FragmentMedFormBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedFormFragment : Fragment() {
    private var _binding: FragmentMedFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedFormViewModel by viewModels()

    private var selectedHour: Int = 8
    private var selectedMinute: Int = 0
    private val selectedDays = mutableSetOf(1, 2, 3, 4, 5, 6, 7)

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

        setupInitialUI()
        setupActions()
        observeState()
        if (medicationId.isNotEmpty()) {
            viewModel.loadMedication(medicationId)
        }
    }

    private fun setupInitialUI() {
        updateTimeDisplay()

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        binding.etMedStartDate.setText(dateFormat.format(Date()))

        val dayViews = listOf(
            binding.tvDay1 to 1,
            binding.tvDay2 to 2,
            binding.tvDay3 to 3,
            binding.tvDay4 to 4,
            binding.tvDay5 to 5,
            binding.tvDay6 to 6,
            binding.tvDay7 to 7
        )

        dayViews.forEach { (view, dayNumber) ->
            view.isSelected = selectedDays.contains(dayNumber)
            view.setOnClickListener {
                if (selectedDays.contains(dayNumber)) {
                    if (selectedDays.size > 1) {
                        selectedDays.remove(dayNumber)
                        view.isSelected = false
                    } else {
                        Toast.makeText(requireContext(), "Minimal pilih 1 hari", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    selectedDays.add(dayNumber)
                    view.isSelected = true
                }
            }
        }
    }

    private fun updateTimeDisplay() {
        binding.etMedHour.setText(selectedHour.toString())
        binding.etMedMinute.setText(selectedMinute.toString())

        val amPm = if (selectedHour >= 12) "PM" else "AM"
        val displayHour = when {
            selectedHour == 0 -> 12
            selectedHour > 12 -> selectedHour - 12
            else -> selectedHour
        }
        val formattedTime = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, selectedMinute, amPm)
        binding.etMedTime.setText(formattedTime)
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(selectedHour)
            .setMinute(selectedMinute)
            .setTitleText("Pilih Jam Minum Obat")
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedHour = timePicker.hour
            selectedMinute = timePicker.minute
            updateTimeDisplay()
        }

        timePicker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun showDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(MaterialDatePicker.todayInUtcMilliseconds()))

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Mulai Tanggal")
            .setCalendarConstraints(constraintsBuilder.build())
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            binding.etMedStartDate.setText(dateFormat.format(Date(selection)))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupActions() {
        binding.etMedTime.setOnClickListener { showTimePicker() }
        binding.tilMedTime.setOnClickListener { showTimePicker() }

        binding.etMedStartDate.setOnClickListener { showDatePicker() }
        binding.tilMedStartDate.setOnClickListener { showDatePicker() }

        binding.btnSaveMedication.setOnClickListener {
            val startDateStr = binding.etMedStartDate.text?.toString().orEmpty()
            if (startDateStr.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    val selectedDate = format.parse(startDateStr)
                    if (selectedDate != null) {
                        val startCal = Calendar.getInstance().apply {
                            time = selectedDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val todayCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (startCal.before(todayCal)) {
                            Toast.makeText(requireContext(), "Tanggal mulai obat tidak boleh sebelum hari ini.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            viewModel.saveMedication(
                name = binding.etMedName.text?.toString().orEmpty(),
                dosage = binding.etMedDosage.text?.toString().orEmpty(),
                hourText = selectedHour.toString(),
                minuteText = selectedMinute.toString(),
                startDate = startDateStr,
                isRecurringForever = binding.switchRecurringForever.isChecked,
                repeatDays = selectedDays.toList().sorted()
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
                selectedHour = medication.intakeHour
                selectedMinute = medication.intakeMinute
                updateTimeDisplay()

                if (medication.startDate.isNotEmpty()) {
                    binding.etMedStartDate.setText(medication.startDate)
                }
                binding.switchRecurringForever.isChecked = medication.isRecurringForever

                if (medication.repeatDays.isNotEmpty()) {
                    selectedDays.clear()
                    selectedDays.addAll(medication.repeatDays)
                    listOf(
                        binding.tvDay1 to 1,
                        binding.tvDay2 to 2,
                        binding.tvDay3 to 3,
                        binding.tvDay4 to 4,
                        binding.tvDay5 to 5,
                        binding.tvDay6 to 6,
                        binding.tvDay7 to 7
                    ).forEach { (view, dayNumber) ->
                        view.isSelected = selectedDays.contains(dayNumber)
                    }
                }
            }
        }

        viewModel.isEditing.observe(viewLifecycleOwner) { isEditing ->
            binding.tvMedFormTitle.text = if (isEditing) "Edit Jadwal Obat" else "Tambah Jadwal Obat"
            binding.btnSaveMedication.text = if (isEditing) "Perbarui Jadwal Obat" else "Simpan Jadwal Obat"
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
