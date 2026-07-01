package com.mdp.caremate.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Medication(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val dosage: String = "",
    val intakeHour: Int = 0,
    val intakeMinute: Int = 0,
    @get:PropertyName("isTakenToday")
    @set:PropertyName("isTakenToday")
    var isTakenToday: Boolean = false,
    @get:PropertyName("isEnabled")
    @set:PropertyName("isEnabled")
    var isEnabled: Boolean = true,
    val startDate: String = "",
    @get:PropertyName("isRecurringForever")
    @set:PropertyName("isRecurringForever")
    var isRecurringForever: Boolean = true,
    val repeatDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    @get:PropertyName("photoUrl")
    @set:PropertyName("photoUrl")
    var photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
