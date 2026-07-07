package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.sources.remote.FirebaseSource
import kotlinx.coroutines.launch
import java.util.UUID

class EventFormViewModel : ViewModel() {

    private val firebaseSource = FirebaseSource()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _isActionSuccess = MutableLiveData<Boolean>()
    val isActionSuccess: LiveData<Boolean> get() = _isActionSuccess

    fun saveEvent(
        currentEventId: String?,
        name: String,
        date: String,
        time: String,
        place: String,
        capacity: String,
        selectedTimestamp: Long,
        targetListedStatus: Boolean // 1. Tambahkan parameter status baru dari fragment di sini
    ) {
        // Validasi input kosong
        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || place.isEmpty() || capacity.isEmpty()) {
            _toastMessage.value = "Semua field harus diisi!"
            return
        }

        // Validasi Waktu: Hanya mengunci jika TAMBAH BARU (currentEventId == null)
        if (currentEventId == null && selectedTimestamp < System.currentTimeMillis()) {
            _toastMessage.value = "Tanggal dan waktu event tidak boleh sebelum waktu sekarang!"
            return
        }

        // Jika currentEventId null, buat ID baru. Jika ada, pakai ID lama (proses EDIT).
        val id = currentEventId ?: UUID.randomUUID().toString()

        val eventData = Event(
            eid = id,
            name = name,
            date = date,
            time = time,
            place = place,
            capacity = capacity,
            listed = targetListedStatus // 2. Set nilainya dinamis sesuai parameter targetListedStatus
        )

        viewModelScope.launch {
            val isSuccess = firebaseSource.saveEvent(eventData)
            if (isSuccess) {
                // Berikan pesan toast yang adaptif agar admin tahu status terbarunya
                val statusText = if (targetListedStatus) "dipublish" else "disimpan"
                _toastMessage.value = "Event '${eventData.name}' berhasil $statusText"
                _isActionSuccess.value = true
            } else {
                _toastMessage.value = "Gagal menyimpan"
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            val isSuccess = firebaseSource.deleteEvent(eventId)
            if (isSuccess) {
                _toastMessage.value = "Event berhasil dihapus"
                _isActionSuccess.value = true
            } else {
                _toastMessage.value = "Gagal menghapus event"
                _isActionSuccess.value = false
            }
        }
    }
}