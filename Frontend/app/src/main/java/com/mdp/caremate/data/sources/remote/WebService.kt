package com.mdp.caremate.data.sources.remote

import retrofit2.http.Body
import retrofit2.http.POST

import retrofit2.http.GET
import retrofit2.http.Path
import com.mdp.caremate.data.model.Event

interface WebService {
    // Endpoint untuk AI Mindful Journaling
    // Mengirim teks jurnal, menerima hasil analisis (skor stres & saran)
    @POST("premium/analyze-mood")
    suspend fun analyzeMood(@Body request: MoodRequestJson): JournalJson

    // Endpoint untuk AI Smart Nutrition
    // Mengirim nama makanan atau link foto, menerima data kalori
    @POST("premium/analyze-nutrition")
    suspend fun analyzeNutrition(@Body request: NutritionRequestJson): NutritionJson
    
    // Endpoint untuk AI Vision Medication Verification
    // Mengirim base64 gambar dan nama obat yang diharapkan
    @POST("premium/verify-medication")
    suspend fun verifyMedication(@Body request: MedicationVerifyRequestJson): MedicationVerifyResponseJson

    // Endpoint untuk mengambil seluruh daftar event komunitas
    @GET("api/events")
    suspend fun getAllEvents(): List<Event>

    // Endpoint untuk mengambil detail event komunitas berdasarkan ID
    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: String): Event
}