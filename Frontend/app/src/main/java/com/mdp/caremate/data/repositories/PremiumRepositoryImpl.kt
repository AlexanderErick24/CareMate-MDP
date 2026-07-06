package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition
import com.mdp.caremate.data.repositories.PremiumRepository
import com.mdp.caremate.data.sources.local.JournalDao
import com.mdp.caremate.data.sources.local.toEntity
import com.mdp.caremate.data.sources.remote.ChatHistoryItem
import com.mdp.caremate.data.sources.remote.PremiumRemoteDataSource

class PremiumRepositoryImpl(
    private val remoteDataSource: PremiumRemoteDataSource,
    private val journalDao: JournalDao,
    private val firebaseSource: com.mdp.caremate.data.sources.remote.FirebaseSource = com.mdp.caremate.data.sources.remote.FirebaseSource()
) : PremiumRepository {

    // FUNGSI REMOTE (INTERNET / AI)

    override suspend fun analyzeMood(content: String, history: List<ChatHistoryItem>): Journal {
        return remoteDataSource.analyzeMood(content, history)
    }
    override suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition {
        return remoteDataSource.analyzeNutrition(foodPhotoUri)
    }
    
    override suspend fun verifyMedication(imageBase64: String, expectedMedication: String): com.mdp.caremate.data.model.AiAlert {
        return remoteDataSource.verifyMedication(imageBase64, expectedMedication)
    }
    
    // FUNGSI LOKAL (ROOM DATABASE)
    override suspend fun insertJournal(journal: Journal) {
        // Ingat konsep ganti baju? Kita ubah Journal murni menjadi JournalEntity sebelum masuk Room
        journalDao.insertJournal(journal.toEntity())
    }

    override suspend fun getAllJournals(caregiverId: String): List<Journal> {
        // Ambil data berseragam (Entity) dari Room
        val entities = journalDao.getJournalsByCaregiver(caregiverId)

        // Ubah seluruh daftar menjadi baju kasual (Domain Model) agar bisa dibaca UI
        return entities.map { it.toDomain() }
    }

    override suspend fun saveAiAlert(alert: com.mdp.caremate.data.model.AiAlert): Result<Unit> {
        return firebaseSource.saveAiAlert(alert)
    }

    override suspend fun getAiAlertsForFamily(): Result<List<com.mdp.caremate.data.model.AiAlert>> {
        return firebaseSource.getAiAlertsForFamily()
    }
}