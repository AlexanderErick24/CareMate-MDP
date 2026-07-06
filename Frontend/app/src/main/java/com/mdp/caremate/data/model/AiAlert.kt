package com.mdp.caremate.data.model

data class AiAlert(
    val id: String = "",
    val caregiverId: String = "",
    val title: String = "",
    val description: String = "",
    val severity: String = "", // SAFE, LOW, MEDIUM, HIGH
    val timestamp: Long = 0L,
    val imageUrl: String? = null // if using Firebase Storage later
)
