package com.mdp.caremate.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import com.mdp.caremate.data.model.MedicationHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<MedicationHistory>()

    fun submitList(newItems: List<MedicationHistory>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = items[position]
        holder.tvMedName.text = history.medicationName
        
        // Format time
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        holder.tvTime.text = format.format(Date(history.takenAt))
    }

    override fun getItemCount(): Int = items.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMedName: TextView = itemView.findViewById(R.id.tvHistoryMedName)
        val tvTime: TextView = itemView.findViewById(R.id.tvHistoryTime)
    }
}
