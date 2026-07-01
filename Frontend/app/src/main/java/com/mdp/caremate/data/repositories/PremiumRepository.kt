package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition
import com.mdp.caremate.data.sources.local.JournalDao
import com.mdp.caremate.data.sources.local.JournalEntity
import com.mdp.caremate.data.sources.remote.PremiumRemoteDataSource

interface PremiumRepository {
    suspend fun analyzeMood(content: String): Journal
    suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition
    suspend fun insertJournal(journal: Journal)
    suspend fun getAllJournals(caregiverId: String): List<Journal>
}