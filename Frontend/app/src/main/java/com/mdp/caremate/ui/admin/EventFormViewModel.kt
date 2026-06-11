package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mdp.caremate.data.model.Event
import java.util.UUID

class EventFormViewModel : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _isActionSuccess = MutableLiveData<Boolean>()
    val isActionSuccess: LiveData<Boolean> get() = _isActionSuccess

    /**
     * Memvalidasi input dan memetakan data ke Objek Event sebelum disimpan
     */
    fun saveEvent(
        currentEventId: String?,
        name: String,
        date: String,
        time: String,
        place: String,
        capacity: String
    ) {
        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || place.isEmpty() || capacity.isEmpty()) {
            _toastMessage.value = "Semua field harus diisi!"
            return
        }

        // Generate ID baru jika tambah data, gunakan ID lama jika mode edit
        val id = currentEventId ?: UUID.randomUUID().toString()

        val eventData = Event(
            eid = id,
            name = name,
            date = date,
            time = time,
            place = place,
            capacity = capacity,
            listed = true
        )

        // TODO: Hubungkan ke Repository / Firebase / Room Anda di sini
        // repository.insertOrUpdateEvent(eventData)

        _toastMessage.value = "Event '${eventData.name}' berhasil disimpan"
        _isActionSuccess.value = true
    }

    /**
     * Logika untuk menghapus Event berdasarkan ID
     */
    fun deleteEvent(currentEventId: String?) {
        if (currentEventId == null) {
            _toastMessage.value = "Gagal menghapus: ID Event tidak ditemukan!"
            return
        }

        // TODO: Hubungkan ke Repository / Firebase / Room untuk menghapus data
        // repository.deleteEvent(currentEventId)

        _toastMessage.value = "Event berhasil dihapus"
        _isActionSuccess.value = true
    }
}