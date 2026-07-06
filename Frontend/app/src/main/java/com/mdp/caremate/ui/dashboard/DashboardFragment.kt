package com.mdp.caremate.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.databinding.FragmentDashboardBinding
import com.mdp.caremate.ui.medicationform.MedFormFragment
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Base64
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.mdp.caremate.data.model.MedicationHistory
import com.mdp.caremate.databinding.DialogMedicationPreviewBinding
import com.mdp.caremate.databinding.DialogUploadPhotoBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var medicationAdapter: MedicationAdapter
    private var allMedications: List<Medication> = emptyList()
    private var allHistory: List<MedicationHistory> = emptyList()
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private val calendarDays = mutableListOf<Calendar>()

    private var currentMedicationForPhoto: Medication? = null
    private var currentPreviewBinding: DialogMedicationPreviewBinding? = null

    private val getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val med = currentMedicationForPhoto ?: return@registerForActivityResult
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (originalBitmap != null) {
                    val maxDim = 600
                    val scale = maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                    val scaledBitmap = if (scale < 1f) {
                        Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
                    } else {
                        originalBitmap
                    }
                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                    med.photoUrl = base64String
                    viewModel.updateMedicationTakenStatus(med, true, base64String)

                    currentPreviewBinding?.let { prevBinding ->
                        prevBinding.ivPreviewPhoto.setImageBitmap(scaledBitmap)
                        prevBinding.tvNoPhoto.isVisible = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
        setupCalendarStrip()
        setupSwipeToDelete()
        observeMedicationList()
        setupSearch()
        
        binding.btnHistory.setOnClickListener {
            HistoryBottomSheetFragment().show(childFragmentManager, HistoryBottomSheetFragment.TAG)
        }
    }

    private fun setupCalendarStrip() {
        calendarDays.clear()
        val today = Calendar.getInstance()
        
        // Generate 7 days: 3 days before today, today, 3 days after today
        for (offset in -3..3) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset)
            calendarDays.add(cal)
        }

        val dateCards = listOf(
            binding.cardDate1 to (binding.tvDateNum1 to binding.tvDayName1),
            binding.cardDate2 to (binding.tvDateNum2 to binding.tvDayName2),
            binding.cardDate3 to (binding.tvDateNum3 to binding.tvDayName3),
            binding.cardDate4 to (binding.tvDateNum4 to binding.tvDayName4),
            binding.cardDate5 to (binding.tvDateNum5 to binding.tvDayName5),
            binding.cardDate6 to (binding.tvDateNum6 to binding.tvDayName6),
            binding.cardDate7 to (binding.tvDateNum7 to binding.tvDayName7)
        )

        dateCards.forEachIndexed { index, (cardView, textViews) ->
            val cal = calendarDays[index]
            val (tvNum, tvName) = textViews

            tvNum.text = cal.get(Calendar.DAY_OF_MONTH).toString()
            tvName.text = if (isSameDay(cal, today)) {
                "Hari Ini"
            } else {
                getIndonesianShortDayName(cal)
            }

            cardView.setOnClickListener {
                selectedCalendar = cal.clone() as Calendar
                updateCalendarStripSelection()
                applyFilterAndRender()
            }
        }

        updateCalendarStripSelection()
    }

    private fun updateCalendarStripSelection() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        binding.tvDashboardSubtitle.text = dateFormat.format(selectedCalendar.time)

        val dateCards = listOf(
            binding.cardDate1 to (binding.tvDateNum1 to binding.tvDayName1),
            binding.cardDate2 to (binding.tvDateNum2 to binding.tvDayName2),
            binding.cardDate3 to (binding.tvDateNum3 to binding.tvDayName3),
            binding.cardDate4 to (binding.tvDateNum4 to binding.tvDayName4),
            binding.cardDate5 to (binding.tvDateNum5 to binding.tvDayName5),
            binding.cardDate6 to (binding.tvDateNum6 to binding.tvDayName6),
            binding.cardDate7 to (binding.tvDateNum7 to binding.tvDayName7)
        )

        val primaryColor = ContextCompat.getColor(requireContext(), R.color.caremate_primary)
        val neutralColor = ContextCompat.getColor(requireContext(), R.color.caremate_neutral)
        val mutedColor = ContextCompat.getColor(requireContext(), R.color.caremate_muted)

        dateCards.forEachIndexed { index, (cardView, textViews) ->
            val cal = calendarDays[index]
            val (tvNum, tvName) = textViews

            if (isSameDay(cal, selectedCalendar)) {
                cardView.setCardBackgroundColor(primaryColor)
                cardView.strokeWidth = 0
                tvNum.setTextColor(Color.WHITE)
                tvName.setTextColor(Color.WHITE)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
                cardView.strokeWidth = 1
                tvNum.setTextColor(neutralColor)
                tvName.setTextColor(mutedColor)
            }
        }
    }

    private fun setupMedicationList() {
        medicationAdapter = MedicationAdapter(
            onMedicationChecked = { medication, isTakenToday ->
                if (!isSameDay(selectedCalendar, Calendar.getInstance())) {
                    Toast.makeText(requireContext(), "Status minum obat hanya dapat diubah untuk jadwal hari ini.", Toast.LENGTH_SHORT).show()
                    medicationAdapter.notifyDataSetChanged()
                    return@MedicationAdapter
                }
                if (isTakenToday) {
                    showUploadPhotoDialog(medication)
                } else {
                    viewModel.updateMedicationTakenStatus(medication, false)
                }
            },
            onMedicationEdit = { medication ->
                openMedicationForm(medication.id)
            },
            onMedicationCardClick = { medication ->
                showMedicationDetailDialog(medication)
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
            allMedications = medications
            applyFilterAndRender()
        }
        viewModel.historyList.observe(viewLifecycleOwner) { history ->
            allHistory = history
            applyFilterAndRender()
        }
    }

    private fun applyFilterAndRender() {
        val selectedDayNumber = getIndonesianDayOfWeekNumber(selectedCalendar)
        val isToday = isSameDay(selectedCalendar, Calendar.getInstance())
        
        val filteredByDate = allMedications.filter { medication ->
            val matchesDay = medication.repeatDays.isEmpty() || medication.repeatDays.contains(selectedDayNumber)
            
            val isAfterStart = if (medication.startDate.isNotEmpty()) {
                try {
                    val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    val start = format.parse(medication.startDate)
                    if (start != null) {
                        val startCal = Calendar.getInstance().apply { time = start }
                        !isBeforeDay(selectedCalendar, startCal)
                    } else true
                } catch (e: Exception) {
                    true
                }
            } else true

            matchesDay && isAfterStart
        }.map { medication ->
            if (isToday) {
                medication
            } else {
                val historyForDay = allHistory.find { history ->
                    val historyCal = Calendar.getInstance().apply { timeInMillis = history.takenAt }
                    history.medicationId == medication.id && isSameDay(historyCal, selectedCalendar)
                }
                if (historyForDay != null) {
                    medication.copy(isTakenToday = true, photoUrl = historyForDay.photoUrl)
                } else {
                    medication.copy(isTakenToday = false, photoUrl = "")
                }
            }
        }

        medicationAdapter.setMedications(filteredByDate)
        
        binding.rvMedications.isVisible = filteredByDate.isNotEmpty()
        binding.tvEmptyState.isVisible = filteredByDate.isEmpty()
    }

    private fun setupSearch() {
        binding.etDashboardSearch.doOnTextChanged { text, _, _, _ ->
            medicationAdapter.filter(text?.toString().orEmpty())
        }
        binding.fabAddMedication.setOnClickListener {
            findNavController().navigate(R.id.action_dest_dashboard_to_dest_med_form)
        }
    }

    private fun openMedicationForm(medicationId: String?) {
        val bundle = Bundle().apply {
            if (medicationId != null) {
                putString(MedFormFragment.ARG_MEDICATION_ID, medicationId)
            }
        }
        findNavController().navigate(R.id.action_dest_dashboard_to_dest_med_form, bundle)
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

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isBeforeDay(cal1: Calendar, cal2: Calendar): Boolean {
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false
        return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getIndonesianDayOfWeekNumber(cal: Calendar): Int {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun getIndonesianShortDayName(cal: Calendar): String {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Min"
            Calendar.MONDAY -> "Sen"
            Calendar.TUESDAY -> "Sel"
            Calendar.WEDNESDAY -> "Rab"
            Calendar.THURSDAY -> "Kam"
            Calendar.FRIDAY -> "Jum"
            Calendar.SATURDAY -> "Sab"
            else -> ""
        }
    }

    private fun showUploadPhotoDialog(medication: Medication) {
        val dialogBinding = DialogUploadPhotoBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogBinding.btnSelectPhoto.setOnClickListener {
            currentMedicationForPhoto = medication
            currentPreviewBinding = null
            dialog.dismiss()
            getContentLauncher.launch("image/*")
        }

        dialogBinding.btnCancelDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMedicationDetailDialog(medication: Medication) {
        val dialogBinding = DialogMedicationPreviewBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogBinding.tvPreviewName.text = medication.name
        dialogBinding.tvPreviewDosage.text = "Dosis: ${medication.dosage}"
        val timeStr = if (medication.intakeHour in 0..23 && medication.intakeMinute in 0..59) {
            String.format(Locale.getDefault(), "%02d:%02d", medication.intakeHour, medication.intakeMinute)
        } else {
            "Jam belum diatur"
        }
        dialogBinding.tvPreviewTime.text = "⏰ $timeStr"

        if (medication.photoUrl.isNotEmpty()) {
            dialogBinding.tvNoPhoto.isVisible = false
            try {
                val decodedBytes = Base64.decode(medication.photoUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    dialogBinding.ivPreviewPhoto.setImageBitmap(bitmap)
                } else {
                    dialogBinding.ivPreviewPhoto.load(medication.photoUrl)
                }
            } catch (e: Exception) {
                dialogBinding.ivPreviewPhoto.load(medication.photoUrl)
            }
        } else {
            dialogBinding.tvNoPhoto.isVisible = true
            dialogBinding.ivPreviewPhoto.setImageDrawable(null)
        }

        dialogBinding.btnUploadNewPhoto.setOnClickListener {
            currentMedicationForPhoto = medication
            currentPreviewBinding = dialogBinding
            getContentLauncher.launch("image/*")
        }

        dialogBinding.btnCloseDialog.setOnClickListener {
            currentPreviewBinding = null
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (currentPreviewBinding == dialogBinding) {
                currentPreviewBinding = null
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        binding.rvMedications.adapter = null
        _binding = null
        super.onDestroyView()
    }
}