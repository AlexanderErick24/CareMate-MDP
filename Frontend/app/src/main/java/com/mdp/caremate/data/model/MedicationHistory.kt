package com.mdp.caremate.data.model

import com.google.firebase.firestore.DocumentId

data class MedicationHistory(
    val id: String = "",
    val medicationId: String = "",
    val medicationName: String = "",
    val takenAt: Long = 0L,
    val photoUrl: String = ""
)
