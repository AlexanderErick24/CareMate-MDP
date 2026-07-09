//package com.mdp.caremate.ui.admin
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.mdp.caremate.data.model.Event
//import com.mdp.caremate.databinding.ItemEventBinding // Sesuaikan dengan nama file XML item event kamu
//
//class EventAdapter(
//    private var eventList: List<Event>,
//    private val onEditClick: (Event) -> Unit,
//    private val onPesertaClick: (Event) -> Unit
//) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
//
//    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(event: Event) {
//            binding.tvEventTitle.text = event.name
//            binding.tvEventLocation.text = event.place
//
//            // Menggabungkan tanggal dan waktu
//            val dateTimeText = "${event.date} • ${event.time} WIB"
//            binding.tvEventDate.text = dateTimeText
//
//            // Set text kapasitas peserta (contoh dinamis menggunakan field capacity)
//            binding.btnPeserta.text = "Peserta (0/${event.capacity})"
//
//            // Status Badge berdasarkan attribute listed
//            if (event.listed) {
//                binding.tvStatus.text = "Listed"
//                // Anda bisa menyesuaikan warna atau drawable di sini jika diperlukan
//            } else {
//                binding.tvStatus.text = "Draft"
//            }
//
//            // Aksi Klik Tombol
//            binding.btnEdit.setOnClickListener { onEditClick(event) }
//            binding.btnPeserta.setOnClickListener { onPesertaClick(event) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
//        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return EventViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
//        holder.bind(eventList[position])
//    }
//
//    override fun getItemCount(): Int = eventList.size
//
//    // Fungsi untuk memperbarui data setelah difilter atau di-fetch ulang
//    fun updateList(newList: List<Event>) {
//        this.eventList = newList
//        notifyDataSetChanged()
//    }
//}

package com.mdp.caremate.ui.admin

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.ItemEventBinding

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

            // Set text kapasitas peserta
            binding.btnPeserta.text = "Kapasitas: ${event.capacity}"

            // --- LOGIKA MENAMPILKAN FOTO EVENT (BASE64) ---
            if (!event.photoUrl.isNullOrEmpty()) {
                try {
                    // Decode string Base64 menjadi byte array
                    val decodedBytes = Base64.decode(event.photoUrl, Base64.DEFAULT)
                    // Convert byte array menjadi Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    if (bitmap != null) {
                        binding.imgEventPoster.setImageBitmap(bitmap)
                    } else {
                        // Jika bukan Base64 mentah, coba load langsung via Coil (misal URL biasa)
                        binding.imgEventPoster.load(event.photoUrl)
                    }
                } catch (e: Exception) {
                    // Fallback jika proses decode error
                    binding.imgEventPoster.load(event.photoUrl)
                }
            } else {
                // Gambar placeholder jika photoUrl kosong/null (bisa diganti drawable punyamu)
                binding.imgEventPoster.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            // ----------------------------------------------

            // Status Badge berdasarkan attribute listed
            if (event.listed) {
                binding.tvStatus.text = "Listed"
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

    fun updateList(newList: List<Event>) {
        this.eventList = newList
        notifyDataSetChanged()
    }
}