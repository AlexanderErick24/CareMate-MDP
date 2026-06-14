package com.mdp.caremate.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.ItemEventBinding // Pastikan Anda sudah membuat layout item_event.xml

class EventAdapter(
    private val listEvent: ArrayList<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            // Sesuaikan ID TextView di bawah ini dengan ID asli yang ada di file item_event.xml Anda
            // Contoh implementasi:
            // binding.tvItemTitle.text = event.name
            // binding.tvItemDate.text = "${event.date} | ${event.time}"
            // binding.tvItemLocation.text = event.place

            binding.root.setOnClickListener {
                onItemClick(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(listEvent[position])
    }

    override fun getItemCount(): Int = listEvent.size
}