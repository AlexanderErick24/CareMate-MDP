package com.mdp.caremate.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.ItemEventBinding // Sesuaikan dengan nama file XML item event kamu

class EventAdapter(
    private var eventList: List<Event>,
    private val onEditClick: (Event) -> Unit,
    private val onPesertaClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvEventTitle.text = event.name
            binding.tvEventLocation.text = event.place

            // Menggabungkan tanggal dan waktu
            val dateTimeText = "${event.date} • ${event.time} WIB"
            binding.tvEventDate.text = dateTimeText

            // Set text kapasitas peserta (contoh dinamis menggunakan field capacity)
            binding.btnPeserta.text = "Peserta (0/${event.capacity})"

            // Status Badge berdasarkan attribute listed
            if (event.listed) {
                binding.tvStatus.text = "Listed"
                // Anda bisa menyesuaikan warna atau drawable di sini jika diperlukan
            } else {
                binding.tvStatus.text = "Draft"
            }

            // Aksi Klik Tombol
            binding.btnEdit.setOnClickListener { onEditClick(event) }
            binding.btnPeserta.setOnClickListener { onPesertaClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(eventList[position])
    }

    override fun getItemCount(): Int = eventList.size

    // Fungsi untuk memperbarui data setelah difilter atau di-fetch ulang
    fun updateList(newList: List<Event>) {
        this.eventList = newList
        notifyDataSetChanged()
    }
}