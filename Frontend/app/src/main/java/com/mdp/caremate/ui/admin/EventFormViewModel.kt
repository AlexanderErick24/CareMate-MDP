package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.sources.remote.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EventFormViewModel : ViewModel() {

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
        capacity: String
    ) {
        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || place.isEmpty() || capacity.isEmpty()) {
            _toastMessage.value = "Semua field harus diisi!"
            return
        }

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

        viewModelScope.launch {
            try {
                // Eksekusi pengiriman data ke Node.js backend di I/O Thread
                withContext(Dispatchers.IO) {
                    ApiConfig.getWebService().insertEvent(eventData)
                }

                _toastMessage.value = "Event '${eventData.name}' berhasil disimpan ke database"
                _isActionSuccess.value = true
            } catch (e: Exception) {
                _toastMessage.value = "Gagal menyimpan ke database: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun deleteEvent(currentEventId: String?) {
        if (currentEventId == null) {
            _toastMessage.value = "Gagal menghapus: ID Event tidak ditemukan!"
            return
        }

        viewModelScope.launch {
            try {
                // Eksekusi penghapusan data di I/O Thread
                withContext(Dispatchers.IO) {
                    ApiConfig.getWebService().deleteEvent(currentEventId)
                }

                _toastMessage.value = "Event berhasil dihapus dari database"
                _isActionSuccess.value = true
            } catch (e: Exception) {
                _toastMessage.value = "Gagal menghapus data: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}