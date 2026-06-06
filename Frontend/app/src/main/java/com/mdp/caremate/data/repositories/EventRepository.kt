package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeAllEvents(): Flow<List<Event>>
    suspend fun getEventById(eventId: String): Event?
}
