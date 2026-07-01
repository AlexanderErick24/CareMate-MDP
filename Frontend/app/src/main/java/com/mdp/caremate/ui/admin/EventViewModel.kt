package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.sources.remote.FirebaseSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val firebaseSource = FirebaseSource()

    // Encapsulation: Mengamankan data agar tidak bisa dimodifikasi langsung dari luar
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchEvents() {
        _isLoading.value = true
        _errorMessage.value = null

        // Menjalankan coroutine menggunakan scope bawaan ViewModel (Otomatis dibatalkan jika ViewModel dihancurkan)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val result = firebaseSource.getAllEvent()
                _events.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan saat memuat data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}