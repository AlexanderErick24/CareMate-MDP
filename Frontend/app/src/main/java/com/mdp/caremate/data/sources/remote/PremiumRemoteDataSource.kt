package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition

interface PremiumRemoteDataSource {
    suspend fun analyzeMood(content: String, history: List<ChatHistoryItem> = emptyList()): Journal
    suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition
    suspend fun verifyMedication(imageBase64: String, expectedMedication: String): com.mdp.caremate.data.model.AiAlert
}