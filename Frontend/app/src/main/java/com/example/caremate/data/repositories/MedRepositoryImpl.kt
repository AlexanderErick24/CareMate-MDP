package com.example.caremate.data.repositories

import com.example.caremate.data.model.Medication
import com.example.caremate.data.model.toMedication
import com.example.caremate.data.model.toMedicationEntity
import com.example.caremate.data.sources.local.MedicationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedRepositoryImpl(
    private val medicationDao: MedicationDao
) : MedRepository {
    override fun observeAllMedications(): Flow<List<Medication>> {
        return medicationDao.observeAllMedications().map { medications ->
            medications.map { it.toMedication() }
        }
    }

    override fun observeTodaysMedications(): Flow<List<Medication>> {
        return medicationDao.observeTodaysMedications().map { medications ->
            medications.map { it.toMedication() }
        }
    }

    override suspend fun getMedicationById(medicationId: Long): Medication? {
        return medicationDao.getMedicationById(medicationId)?.toMedication()
    }

    override suspend fun insertMedication(medication: Medication): Medication {
        val now = System.currentTimeMillis()
        val normalizedMedication = medication.copy(
            id = 0L,
            updatedAt = now
        )
        val insertedId = medicationDao.insertMedication(normalizedMedication.toMedicationEntity())
        return normalizedMedication.copy(id = insertedId)
    }

    override suspend fun updateMedication(medication: Medication): Medication {
        val existingMedication = medicationDao.getMedicationById(medication.id)
            ?: throw NoSuchElementException("Medication with ID ${medication.id} not found")
        val updatedMedication = medication.copy(
            id = existingMedication.id,
            createdAt = existingMedication.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        medicationDao.updateMedication(updatedMedication.toMedicationEntity())
        return updatedMedication
    }

    override suspend fun deleteMedication(medication: Medication): Medication {
        val existingMedication = medicationDao.getMedicationById(medication.id)
            ?: throw NoSuchElementException("Medication with ID ${medication.id} not found")
        medicationDao.deleteMedication(existingMedication)
        return existingMedication.toMedication()
    }

    override suspend fun deleteMedicationById(medicationId: Long): Medication {
        val existingMedication = medicationDao.getMedicationById(medicationId)
            ?: throw NoSuchElementException("Medication with ID $medicationId not found")
        medicationDao.deleteMedication(existingMedication)
        return existingMedication.toMedication()
    }

    override suspend fun setMedicationTakenStatus(
        medicationId: Long,
        isTakenToday: Boolean
    ): Medication {
        val existingMedication = medicationDao.getMedicationById(medicationId)
            ?: throw NoSuchElementException("Medication with ID $medicationId not found")
        val updatedAt = System.currentTimeMillis()
        medicationDao.updateTakenStatus(medicationId, isTakenToday, updatedAt)
        return existingMedication.toMedication().copy(
            isTakenToday = isTakenToday,
            updatedAt = updatedAt
        )
    }

    override suspend fun resetTakenStatus() {
        medicationDao.resetTakenStatus(System.currentTimeMillis())
    }
}
