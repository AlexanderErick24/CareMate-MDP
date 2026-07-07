package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition

// Model untuk satu item riwayat percakapan (sesuai format Gemini)
data class ChatHistoryPart(val text: String)
data class ChatHistoryItem(val role: String, val parts: List<ChatHistoryPart>)

// Data yang dikirim dari Android ke Backend Node.js
data class MoodRequestJson(
    val content: String,
    val history: List<ChatHistoryItem> = emptyList()
)

// Data yang diterima Android dari Backend Node.js
data class JournalJson(
    val id: String,
    val content: String,
    val moodScore: Int,
    val aiAnalysis: String,
    val timestamp: Long
) {
    // Fungsi bantuan untuk mengubah model JSON menjadi model murni Aplikasi (Domain Model)
    fun toDomain(): Journal {
        return Journal(
            id = this.id,
            content = this.content,
            moodScore = this.moodScore,
            aiAnalysis = this.aiAnalysis,
            timestamp = this.timestamp
        )
    }
}
// ==========================================
// 2. MODEL DATA UNTUK SMART NUTRITION
// ==========================================

// Data yang dikirim dari Android ke Backend Node.js
data class NutritionRequestJson(
    val foodPhotoUri: String
)

// Data yang diterima Android dari Backend Node.js
data class NutritionJson(
    val foodName: String,
    val calories: Int,
    val aiRecommendation: String
) {
    // Fungsi bantuan untuk mengubah model JSON menjadi model murni Aplikasi (Domain Model)
    fun toDomain(): Nutrition {
        return Nutrition(
            foodName = this.foodName,
            calories = this.calories,
            aiRecommendation = this.aiRecommendation
        )
    }
}
