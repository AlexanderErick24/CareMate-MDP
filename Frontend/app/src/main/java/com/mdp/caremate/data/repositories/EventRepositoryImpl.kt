package com.mdp.caremate.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.data.model.Event
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepositoryImpl : EventRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")

    // Menggunakan callbackFlow agar perubahan data di Firestore
    // langsung ter-observe secara realtime oleh UI (seperti Flow dari Room)
    override fun observeAllEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(events)
            }
        // Hentikan listener saat Flow tidak lagi di-observe
        awaitClose { listener.remove() }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = eventsCollection.document(eventId).get().await()
            doc.toObject(Event::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
}
