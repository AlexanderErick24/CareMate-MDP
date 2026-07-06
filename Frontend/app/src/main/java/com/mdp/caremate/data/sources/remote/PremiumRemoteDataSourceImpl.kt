package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition

class PremiumRemoteDataSourceImpl(
    // Berikan nilai default agar langsung mengambil dari ApiConfig
    private val webService: WebService = ApiConfig.getWebService()
) : PremiumRemoteDataSource {

    override suspend fun analyzeMood(content: String, history: List<ChatHistoryItem>): Journal {
        // Bungkus teks + riwayat percakapan menjadi JSON Request
        val request = MoodRequestJson(content = content, history = history)
        // Tembak API menggunakan Retrofit
        val responseJson = webService.analyzeMood(request)

        // Ubah format JSON menjadi format Aplikasi, lalu kembalikan
        return responseJson.toDomain()
    }

    override suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition {
        val request = NutritionRequestJson(foodPhotoUri)
        val responseJson = webService.analyzeNutrition(request)
        return responseJson.toDomain()
    }

    override suspend fun verifyMedication(imageBase64: String, expectedMedication: String): com.mdp.caremate.data.model.AiAlert {
        val request = MedicationVerifyRequestJson(imageBase64, expectedMedication)
        val response = webService.verifyMedication(request)
        return com.mdp.caremate.data.model.AiAlert(
            id = java.util.UUID.randomUUID().toString(),
            title = response.title,
            description = response.description,
            severity = response.severity,
            timestamp = response.timestamp,
            imageUrl = imageBase64 // Pass the base64 string directly
        )
    }
}