package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition

interface PremiumRemoteDataSource {
    suspend fun analyzeMood(content: String): Journal
    suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition
}