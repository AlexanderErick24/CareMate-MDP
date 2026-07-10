package com.mdp.caremate.data.model

data class QuitRequest(
    val requestId: String = "",

    // "caregiver_quit" → caregiver wants to quit patient
    // "family_quit"    → family member wants to quit caregiver
    val type: String = "",

    // "pending" | "approved" | "rejected"
    val status: String = "pending",

    val caregiverUid: String = "",

    // Only filled for family_quit type
    val familyUid: String = "",

    // Only filled when status = "rejected"
    val reason: String = "",

    val createdAt: Long = 0
)