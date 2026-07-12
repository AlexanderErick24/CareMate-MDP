package com.mdp.caremate.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mdp.caremate.data.model.Medication
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class MedRepositoryImpl : MedRepository {

    private val firestore = FirebaseFirestore.getInstance()

    override fun observeAllMedications(targetUid: String): Flow<List<Medication>> = callbackFlow {

        // FIX: kalau targetUid kosong (misal setelah quit), langsung emit list kosong dan stop
        // supaya tidak crash dengan "Invalid document reference"
        if (targetUid.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val collection = firestore.collection("users").document(targetUid).collection("medications")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val medications = snapshot.documents.mapNotNull { it.toObject(Medication::class.java) }
                trySend(medications)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun observeTodaysMedications(targetUid: String): Flow<List<Medication>> = callbackFlow {

        // FIX: guard yang sama — ini yang menyebabkan crash di log
        if (targetUid.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val collection = firestore.collection("users").document(targetUid).collection("medications")
            .whereEqualTo("isEnabled", true)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val medications = snapshot.documents.mapNotNull { it.toObject(Medication::class.java) }
                    .sortedWith(compareBy({ it.intakeHour }, { it.intakeMinute }))
                trySend(medications)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun observeHistory(targetUid: String): Flow<List<com.mdp.caremate.data.model.MedicationHistory>> = callbackFlow {

        // FIX: sama, guard dulu sebelum akses Firestore
        if (targetUid.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val collection = firestore.collection("users").document(targetUid).collection("medication_history")
            .orderBy("takenAt", Query.Direction.DESCENDING)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val history = snapshot.documents.mapNotNull { it.toObject(com.mdp.caremate.data.model.MedicationHistory::class.java) }
                trySend(history)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getMedicationById(targetUid: String, medicationId: String): Medication? {

        // FIX: suspend function tidak pakai callbackFlow tapi tetap perlu guard
        if (targetUid.isEmpty()) return null

        val doc = firestore.collection("users").document(targetUid)
            .collection("medications").document(medicationId).get().await()
        return doc.toObject(Medication::class.java)
    }

    override suspend fun insertMedication(targetUid: String, medication: Medication): Medication {
        val collection = firestore.collection("users").document(targetUid).collection("medications")
        val newDoc = collection.document()
        val medToSave = medication.copy(
            id = newDoc.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        newDoc.set(medToSave).await()
        return medToSave
    }

    override suspend fun updateMedication(targetUid: String, medication: Medication): Medication {
        val doc = firestore.collection("users").document(targetUid)
            .collection("medications").document(medication.id)
        val medToUpdate = medication.copy(updatedAt = System.currentTimeMillis())
        doc.set(medToUpdate).await()
        return medToUpdate
    }

    override suspend fun deleteMedication(targetUid: String, medication: Medication): Medication {
        return deleteMedicationById(targetUid, medication.id)
    }

    override suspend fun deleteMedicationById(targetUid: String, medicationId: String): Medication {
        val med = getMedicationById(targetUid, medicationId)
            ?: throw NoSuchElementException("Not found")
        firestore.collection("users").document(targetUid)
            .collection("medications").document(medicationId).delete().await()
        return med
    }

    override suspend fun resetTakenStatus(targetUid: String) {

        // FIX: guard untuk suspend function juga
        if (targetUid.isEmpty()) return

        val collection = firestore.collection("users").document(targetUid).collection("medications")
        val snapshot = collection.whereEqualTo("isTakenToday", true).get().await()

        val batch = firestore.batch()
        for (doc in snapshot.documents) {
            batch.update(
                doc.reference,
                "isTakenToday", false,
                "updatedAt", System.currentTimeMillis()
            )
        }
        batch.commit().await()
    }

    override suspend fun setMedicationTakenStatus(
        targetUid: String,
        medicationId: String,
        isTakenToday: Boolean,
        photoUrl: String?
    ): Medication {

        val docRef = firestore.collection("users").document(targetUid)
            .collection("medications").document(medicationId)
        val updatedAt = System.currentTimeMillis()
        val updateMap = mutableMapOf<String, Any>(
            "isTakenToday" to isTakenToday,
            "updatedAt" to updatedAt
        )
        if (photoUrl != null) {
            updateMap["photoUrl"] = photoUrl
        }
        docRef.update(updateMap).await()

        val updatedMed = getMedicationById(targetUid, medicationId)
            ?: throw Exception("Not found")

        val historyCollection = firestore.collection("users").document(targetUid)
            .collection("medication_history")

        if (isTakenToday) {
            val historyDoc = historyCollection.document()
            val historyData = mapOf(
                "id" to historyDoc.id,
                "medicationId" to medicationId,
                "medicationName" to updatedMed.name,
                "takenAt" to updatedAt,
                "photoUrl" to (photoUrl ?: updatedMed.photoUrl)
            )
            historyDoc.set(historyData).await()
        } else {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val snap = historyCollection
                .whereEqualTo("medicationId", medicationId)
                .get().await()

            val batch = firestore.batch()
            for (d in snap.documents) {
                val takenAt = d.getLong("takenAt") ?: 0L
                if (takenAt >= startOfDay) {
                    batch.delete(d.reference)
                }
            }
            batch.commit().await()
        }

        return updatedMed
    }
}