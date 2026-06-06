package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.model.Nutrition
import com.mdp.caremate.data.sources.local.JournalDao
import com.mdp.caremate.data.sources.local.toEntity
import com.mdp.caremate.data.sources.remote.PremiumRemoteDataSource

class PremiumRepositoryImpl(
    private val remoteDataSource: PremiumRemoteDataSource,
    private val journalDao: JournalDao
) : PremiumRepository {

    // FUNGSI REMOTE (INTERNET / AI)

    override suspend fun analyzeMood(content: String): Journal {
        // Repository tinggal memanggil fungsi dari Data Source
        return remoteDataSource.analyzeMood(content)
    }
    override suspend fun analyzeNutrition(foodPhotoUri: String): Nutrition {
        return remoteDataSource.analyzeNutrition(foodPhotoUri)
    }
    // FUNGSI LOKAL (ROOM DATABASE)
    override suspend fun insertJournal(journal: Journal) {
        // Ingat konsep ganti baju? Kita ubah Journal murni menjadi JournalEntity sebelum masuk Room
        journalDao.insertJournal(journal.toEntity())
    }

    override suspend fun getAllJournals(): List<Journal> {
        // Ambil data berseragam (Entity) dari Room
        val entities = journalDao.getAllJournals()

        // Ubah seluruh daftar menjadi baju kasual (Domain Model) agar bisa dibaca UI
        return entities.map { it.toDomain() }
    }
}