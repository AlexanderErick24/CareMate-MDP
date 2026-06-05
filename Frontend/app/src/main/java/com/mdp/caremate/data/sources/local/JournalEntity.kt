package com.mdp.caremate.data.sources.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mdp.caremate.data.model.Journal

@Entity(tableName = "journal_table")
data class JournalEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val moodScore: Int,
    val aiAnalysis: String,
    val timestamp: Long
) {
    // Ubah Entity menjadi Model Aplikasi
    fun toDomain(): Journal {
        return Journal(id, content, moodScore, aiAnalysis, timestamp)
    }
}
// Fungsi tambahan untuk mengubah Model Aplikasi menjadi Entity
fun Journal.toEntity(): JournalEntity {
    return JournalEntity(id, content, moodScore, aiAnalysis, timestamp)
}
