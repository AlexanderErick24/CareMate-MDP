package com.mdp.caremate.ui.family.alert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AiAlertHistoryAdapter(
    private var alertList: List<AiAlertHistoryItem> = listOf()
) : RecyclerView.Adapter<AiAlertHistoryAdapter.AlertViewHolder>() {

    fun submitList(newList: List<AiAlertHistoryItem>) {
        alertList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ai_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alertList[position])
    }

    override fun getItemCount(): Int = alertList.size

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // We will use standard RecyclerView.ViewHolder as the base
        // Wait, the signature should just be `RecyclerView.ViewHolder`
        
        private val tvAlertTitle: TextView = itemView.findViewById(R.id.tvAlertTitle)
        private val tvAlertDescription: TextView = itemView.findViewById(R.id.tvAlertDescription)
        private val tvAlertTime: TextView = itemView.findViewById(R.id.tvAlertTime)
        private val tvAlertSeverity: TextView = itemView.findViewById(R.id.tvAlertSeverity)
        private var decodeJob: kotlinx.coroutines.Job? = null

        fun bind(item: AiAlertHistoryItem) {
            decodeJob?.cancel()
            tvAlertTitle.text = item.title
            tvAlertDescription.text = item.description
            tvAlertTime.text = item.time
            tvAlertSeverity.text = item.severity

            if (item.severity == "HIGH") {
                tvAlertSeverity.setBackgroundResource(R.drawable.bg_rounded_danger)
            } else if (item.severity == "MEDIUM" || item.severity == "LOW") {
                tvAlertSeverity.setBackgroundResource(R.drawable.bg_rounded_warning)
            } else if (item.severity == "SAFE") {
                tvAlertSeverity.setBackgroundResource(R.drawable.bg_rounded_success)
            } else {
                tvAlertSeverity.setBackgroundResource(R.drawable.bg_rounded_warning) // fallback
            }

            val ivAlertThumbnail: android.widget.ImageView = itemView.findViewById(R.id.ivAlertThumbnail)
            if (!item.imageUrl.isNullOrEmpty()) {
                // Decode in background to prevent UI lag
                decodeJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val decodedBytes = android.util.Base64.decode(item.imageUrl, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                ivAlertThumbnail.setImageBitmap(bitmap)
                                ivAlertThumbnail.imageTintList = null // Clear the gray tint
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                ivAlertThumbnail.setImageResource(android.R.drawable.ic_menu_camera)
                ivAlertThumbnail.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9E9E9E"))
            }
        }
    }
}
