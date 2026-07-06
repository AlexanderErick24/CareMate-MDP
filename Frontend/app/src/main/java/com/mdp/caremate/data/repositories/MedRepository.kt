package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.data.model.MedicationHistory
import kotlinx.coroutines.flow.Flow

interface MedRepository {
    fun observeAllMedications(targetUid: String): Flow<List<Medication>>
    fun observeTodaysMedications(targetUid: String): Flow<List<Medication>>
    fun observeHistory(targetUid: String): Flow<List<MedicationHistory>>

    suspend fun getMedicationById(targetUid: String, medicationId: String): Medication?
    suspend fun insertMedication(targetUid: String, medication: Medication): Medication
    suspend fun updateMedication(targetUid: String, medication: Medication): Medication
    suspend fun deleteMedication(targetUid: String, medication: Medication): Medication
    suspend fun deleteMedicationById(targetUid: String, medicationId: String): Medication
    suspend fun resetTakenStatus(targetUid: String)
    suspend fun setMedicationTakenStatus(
        targetUid: String,
        medicationId: String,
        isTakenToday: Boolean,
        photoUrl: String? = null
    ): Medication
}
