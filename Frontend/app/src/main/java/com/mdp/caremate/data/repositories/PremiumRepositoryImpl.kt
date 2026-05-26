package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition
import com.mdp.caremate.data.sources.remote.PremiumRemoteDataSource

class PremiumRepositoryImpl(
    private val remoteDataSource: PremiumRemoteDataSource
) : PremiumRepository {
    override suspend fun analyzeMood(content: String): Journal {
        // Repository tinggal memanggil fungsi dari Data Source
        return remoteDataSource.analyzeMood(content)
    }

    override suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition {
        return remoteDataSource.analyzeNutrition(foodPhotoUri)
    }
}