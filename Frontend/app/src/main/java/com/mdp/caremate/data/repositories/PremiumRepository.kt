package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition

interface PremiumRepository {
    suspend fun analyzeMood(content: String): Journal
    suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition
}