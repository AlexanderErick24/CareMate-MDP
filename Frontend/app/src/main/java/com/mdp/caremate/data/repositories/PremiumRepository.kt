package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition
import com.mdp.caremate.data.sources.remote.ChatHistoryItem
import com.mdp.caremate.data.sources.local.JournalDao
import com.mdp.caremate.data.sources.local.JournalEntity
import com.mdp.caremate.data.sources.remote.PremiumRemoteDataSource

interface PremiumRepository {
    suspend fun analyzeMood(content: String, history: List<ChatHistoryItem> = emptyList()): Journal
    suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition
    suspend fun verifyMedication(imageBase64: String, expectedMedication: String): com.mdp.caremate.data.model.AiAlert
    suspend fun insertJournal(journal: Journal)
    suspend fun getAllJournals(caregiverId: String): List<Journal>
    suspend fun saveAiAlert(alert: com.mdp.caremate.data.model.AiAlert): Result<Unit>
    suspend fun getAiAlertsForFamily(): Result<List<com.mdp.caremate.data.model.AiAlert>>
}