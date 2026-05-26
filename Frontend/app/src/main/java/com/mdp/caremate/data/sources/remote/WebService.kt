package com.mdp.caremate.data.sources.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface WebService {
    // Endpoint untuk AI Mindful Journaling
    // Mengirim teks jurnal, menerima hasil analisis (skor stres & saran)
    @POST("premium/analyze-mood")
    suspend fun analyzeMood(@Body request: MoodRequestJson): JournalJson

    // Endpoint untuk AI Smart Nutrition
    // Mengirim nama makanan atau link foto, menerima data kalori
    @POST("premium/analyze-nutrition")
    suspend fun analyzeNutrition(@Body request: NutritionRequestJson): NutritionJson
}