package com.mdp.caremate.ui.community

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.ItemEventBinding

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.tvEventTitle.text = event.name
            binding.tvEventDate.text = "${event.date} ${event.time}"
            binding.tvEventLocation.text = event.place
            
            binding.btnEdit.isVisible = false
            val capText = if (event.capacity.contains("/") || event.capacity.contains("Kapasitas", ignoreCase = true)) {
                event.capacity
            } else if (event.capacity.isNotEmpty()) {
                "Kapasitas: ${event.capacity}"
            } else {
                "Terbuka untuk Umum"
            }
            binding.btnPeserta.text = capText
            binding.btnPeserta.isClickable = false

            if (!event.photoUrl.isNullOrEmpty()) {
                binding.imgEventPoster.visibility = View.VISIBLE
                try {
                    val decodedBytes = Base64.decode(event.photoUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        binding.imgEventPoster.setImageBitmap(bitmap)
                    } else {
                        binding.imgEventPoster.load(event.photoUrl)
                    }
                } catch (e: Exception) {
                    binding.imgEventPoster.load(event.photoUrl)
                }
            } else {
                binding.imgEventPoster.visibility = View.GONE
            }
            
            binding.root.setOnClickListener { onEventClick(event) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.eid == newItem.eid
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem == newItem
            }
        }
    }
}
