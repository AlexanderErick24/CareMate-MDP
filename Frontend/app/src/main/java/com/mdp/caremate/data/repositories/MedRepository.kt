package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedRepository {
    fun observeAllMedications(): Flow<List<Medication>>
    fun observeTodaysMedications(): Flow<List<Medication>>

    suspend fun getMedicationById(medicationId: Long): Medication?
    suspend fun insertMedication(medication: Medication): Medication
    suspend fun updateMedication(medication: Medication): Medication
    suspend fun deleteMedication(medication: Medication): Medication
    suspend fun deleteMedicationById(medicationId: Long): Medication
    suspend fun setMedicationTakenStatus(
        medicationId: Long,
        isTakenToday: Boolean
    ): Medication

    suspend fun resetTakenStatus()
}
