package com.mdp.caremate.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.databinding.ItemMedicationBinding
import java.util.Locale

class MedicationAdapter(
    private val onMedicationChecked: (Medication, Boolean) -> Unit,
    private val onMedicationEdit: (Medication) -> Unit,
    private val onMedicationCardClick: (Medication) -> Unit,
    private val onFilteredCountChanged: (Int) -> Unit = {}
) : ListAdapter<Medication, MedicationAdapter.MedicationViewHolder>(DiffCallback) {
    private var allItems: List<Medication> = emptyList()
    private var query: String = ""

    fun setMedications(items: List<Medication>) {
        allItems = items
        applyFilter()
    }

    fun filter(query: String) {
        this.query = query
        applyFilter()
    }

    private fun applyFilter() {
        val normalizedQuery = query.trim()
        val filteredItems = if (normalizedQuery.isEmpty()) {
            allItems
        } else {
            allItems.filter { medication ->
                medication.name.contains(normalizedQuery, ignoreCase = true) ||
                    medication.dosage.contains(normalizedQuery, ignoreCase = true) ||
                    medicationTimeText(medication).contains(normalizedQuery, ignoreCase = true)
            }
        }
        submitList(filteredItems) {
            onFilteredCountChanged(filteredItems.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicationViewHolder(
        private val binding: ItemMedicationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            binding.tvMedicationName.text = medication.name
            binding.tvMedicationDosage.text = medication.dosage
            binding.tvMedicationTime.text = medicationTimeText(medication)
            binding.tvPhotoIndicator.visibility = if (medication.photoUrl.isNotEmpty()) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onMedicationCardClick(medication) }
            binding.cardMedicationItem.setOnClickListener { onMedicationCardClick(medication) }
            binding.btnEditMedication.setOnClickListener { onMedicationEdit(medication) }

            val context = binding.root.context
            if (medication.isTakenToday) {
                binding.cardMedicationItem.setCardBackgroundColor(Color.parseColor("#D9EAFD"))
                binding.btnTakeAction.text = "↩ Batalkan"
                binding.btnTakeAction.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                binding.btnTakeAction.setTextColor(Color.parseColor("#4A4D4C"))
            } else {
                binding.cardMedicationItem.setCardBackgroundColor(Color.WHITE)
                binding.btnTakeAction.text = "Sudah Minum"
                binding.btnTakeAction.backgroundTintList = ContextCompat.getColorStateList(context, R.color.caremate_primary)
                binding.btnTakeAction.setTextColor(Color.WHITE)
            }

            binding.btnTakeAction.setOnClickListener {
                onMedicationChecked(medication, !medication.isTakenToday)
            }
        }
    }

    private fun medicationTimeText(medication: Medication): String {
        return if (medication.intakeHour in 0..23 && medication.intakeMinute in 0..59) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                medication.intakeHour,
                medication.intakeMinute
            )
        } else {
            NO_TIME_TEXT
        }
    }

    companion object {
        private const val NO_TIME_TEXT = "Jam belum diatur"
        private val DiffCallback = object : DiffUtil.ItemCallback<Medication>() {
            override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
                return oldItem == newItem
            }
        }
    }
}
