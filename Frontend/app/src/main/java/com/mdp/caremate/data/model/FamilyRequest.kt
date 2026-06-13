package com.mdp.caremate.data.model

data class FamilyRequest(
    val requestId: String = "",
    val targetCode: String = "",
    val patientUid: String = "",
    val caregiverUid: String = "",
    val caregiverName: String = "",
    val status: String = "pending" // "pending", "accepted", "rejected"
)
