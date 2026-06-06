package com.mdp.caremate.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.repositories.EventRepository
import com.mdp.caremate.data.repositories.EventRepositoryImpl
import kotlinx.coroutines.launch

class EventDetailViewModel : ViewModel() {

    private val eventRepository: EventRepository = EventRepositoryImpl()

    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?> = _event

    // Menerima event langsung dari Bundle (argument) — tidak perlu request ulang ke Firestore
    // untuk data yang sudah ada, cukup set dari luar
    fun setEvent(event: Event) {
        _event.value = event
    }

    // Fallback: ambil dari Firestore jika hanya ada ID (misalnya deep link)
    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _event.value = eventRepository.getEventById(eventId)
        }
    }
}
