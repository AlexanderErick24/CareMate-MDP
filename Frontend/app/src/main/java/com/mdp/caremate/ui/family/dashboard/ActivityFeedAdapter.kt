package com.mdp.caremate.ui.family.dashboard

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

class ActivityFeedAdapter :
    RecyclerView.Adapter<ActivityFeedAdapter.ViewHolder>() {

    private var history =
        emptyList<MedicationHistory>()

    fun submitList(
        newList: List<MedicationHistory>
    ) {

        history = newList

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_activity_feed,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(
            history[position]
        )
    }

    override fun getItemCount() =
        history.size

    class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle =
            itemView.findViewById<TextView>(
                R.id.tvTitle
            )

        private val tvTime =
            itemView.findViewById<TextView>(
                R.id.tvTime
            )

        fun bind(
            history: MedicationHistory
        ) {

            tvTitle.text =
                "${history.medicationName} taken"

            val formattedTime =
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(
                    Date(history.takenAt)
                )

            tvTime.text =
                formattedTime
        }
    }
}