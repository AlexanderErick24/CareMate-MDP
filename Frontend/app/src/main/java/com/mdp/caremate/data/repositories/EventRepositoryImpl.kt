package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.sources.remote.ApiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EventRepositoryImpl : EventRepository {

    private val webService = ApiConfig.getWebService()

    override fun observeAllEvents(): Flow<List<Event>> = flow {
        try {
            val events = webService.getAllEvents()
            emit(events)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            webService.getEventById(eventId)
        } catch (e: Exception) {
            null
        }
    }
}
