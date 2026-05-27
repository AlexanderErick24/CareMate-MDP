package com.mdp.caremate.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.databinding.ItemMedicationBinding
import java.util.Locale

class MedicationAdapter(
    private val onMedicationChecked: (Medication, Boolean) -> Unit,
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

            binding.cbTaken.setOnCheckedChangeListener(null)
            binding.cbTaken.isChecked = medication.isTakenToday
            binding.cbTaken.setOnCheckedChangeListener { _, isChecked ->
                onMedicationChecked(medication, isChecked)
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
