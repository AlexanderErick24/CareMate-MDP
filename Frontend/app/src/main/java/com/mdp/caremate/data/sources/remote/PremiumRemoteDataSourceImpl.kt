package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition

class PremiumRemoteDataSourceImpl(
    // Berikan nilai default agar langsung mengambil dari ApiConfig
    private val webService: WebService = ApiConfig.getWebService()
) : PremiumRemoteDataSource {

    override suspend fun analyzeMood(content: String): Journal {
        // 1. Bungkus teks input menjadi JSON Request (Prompt diatur di sisi Backend)
        val request = MoodRequestJson(content)
        // 2. Tembak API menggunakan Retrofit
        val responseJson = webService.analyzeMood(request)

        // 3. Ubah format JSON menjadi format Aplikasi, lalu kembalikan
        return responseJson.toDomain()
    }

    override suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition {
        val request = NutritionRequestJson(foodPhotoUri)
        val responseJson = webService.analyzeNutrition(request)
        return responseJson.toDomain()
    }
}