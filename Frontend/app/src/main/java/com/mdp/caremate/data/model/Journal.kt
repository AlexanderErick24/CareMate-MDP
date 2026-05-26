package com.mdp.caremate.data.model

data class Journal(
    val id: String = "",
    val content: String = "",
    val moodScore: Int = 0,
    val aiAnalysis: String = "",
    val timestamp: Long = System.currentTimeMillis()
)