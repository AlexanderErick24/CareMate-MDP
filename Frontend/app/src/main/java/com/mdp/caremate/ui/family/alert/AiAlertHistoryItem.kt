package com.mdp.caremate.ui.family.alert

data class AiAlertHistoryItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val severity: String, // "HIGH", "MEDIUM", "LOW"
    val imageUrl: String? = null // null for now as we don't have real images yet
)
