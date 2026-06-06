package com.mdp.caremate.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.repositories.EventRepository
import com.mdp.caremate.data.repositories.EventRepositoryImpl

class CommunityViewModel : ViewModel() {

    private val eventRepository: EventRepository = EventRepositoryImpl()

    val events: LiveData<List<Event>> =
        eventRepository.observeAllEvents().asLiveData()
}
