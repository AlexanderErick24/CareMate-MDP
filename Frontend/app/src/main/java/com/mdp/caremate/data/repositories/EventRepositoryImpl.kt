package com.mdp.caremate.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.data.model.Event
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepositoryImpl : EventRepository {

    private val firestore = FirebaseFirestore.getInstance()

    override fun observeAllEvents(): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("eid") ?: doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: doc.getString("title") ?: ""
                    val date = doc.getString("date") ?: ""
                    val time = doc.getString("time") ?: ""
                    val place = doc.getString("place") ?: doc.getString("location") ?: ""
                    val capacity = doc.getString("capacity") ?: doc.getString("slots") ?: doc.getLong("capacity")?.toString() ?: doc.getLong("slots")?.toString() ?: ""
                    val listed = doc.getBoolean("listed") ?: true
                    Event(
                        eid = id,
                        name = name,
                        date = date,
                        time = time,
                        place = place,
                        capacity = capacity,
                        listed = listed
                    )
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = firestore.collection("events").document(eventId).get().await()
            if (doc.exists()) {
                val id = doc.getString("eid") ?: doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: doc.getString("title") ?: ""
                val date = doc.getString("date") ?: ""
                val time = doc.getString("time") ?: ""
                val place = doc.getString("place") ?: doc.getString("location") ?: ""
                val capacity = doc.getString("capacity") ?: doc.getString("slots") ?: doc.getLong("capacity")?.toString() ?: doc.getLong("slots")?.toString() ?: ""
                val listed = doc.getBoolean("listed") ?: true
                return Event(eid = id, name = name, date = date, time = time, place = place, capacity = capacity, listed = listed)
            }

            val querySnapshot = firestore.collection("events").whereEqualTo("eid", eventId).get().await()
            val firstDoc = querySnapshot.documents.firstOrNull()
            if (firstDoc != null) {
                val id = firstDoc.getString("eid") ?: firstDoc.getString("id") ?: firstDoc.id
                val name = firstDoc.getString("name") ?: firstDoc.getString("title") ?: ""
                val date = firstDoc.getString("date") ?: ""
                val time = firstDoc.getString("time") ?: ""
                val place = firstDoc.getString("place") ?: firstDoc.getString("location") ?: ""
                val capacity = firstDoc.getString("capacity") ?: firstDoc.getString("slots") ?: firstDoc.getLong("capacity")?.toString() ?: firstDoc.getLong("slots")?.toString() ?: ""
                val listed = firstDoc.getBoolean("listed") ?: true
                return Event(eid = id, name = name, date = date, time = time, place = place, capacity = capacity, listed = listed)
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
