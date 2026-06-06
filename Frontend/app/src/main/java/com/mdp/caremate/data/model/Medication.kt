package com.mdp.caremate.data.model

import com.mdp.caremate.data.sources.local.MedicationEntity

data class Medication(
    val id: Long = 0L,
    val name: String,
    val dosage: String,
    val intakeHour: Int,
    val intakeMinute: Int,
    val isTakenToday: Boolean = false,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun MedicationEntity.toMedication(): Medication {
    return Medication(
        id = id,
        name = name,
        dosage = dosage,
        intakeHour = intakeHour,
        intakeMinute = intakeMinute,
        isTakenToday = isTakenToday,
        isEnabled = isEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Medication.toMedicationEntity(): MedicationEntity {
    return MedicationEntity(
        id = id,
        name = name,
        dosage = dosage,
        intakeHour = intakeHour,
        intakeMinute = intakeMinute,
        isTakenToday = isTakenToday,
        isEnabled = isEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
