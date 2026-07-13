package com.mdp.caremate

import android.app.Application
import com.mdp.caremate.data.repositories.PremiumRepository
import com.mdp.caremate.data.repositories.PremiumRepositoryImpl
import com.mdp.caremate.data.sources.local.AppDatabase
import com.mdp.caremate.data.sources.remote.ApiConfig
import com.mdp.caremate.data.sources.remote.PremiumRemoteDataSourceImpl

class CareMateApplication : Application() {

    // Variabel yang dicari oleh PremiumViewModelFactory
    lateinit var premiumRepository: PremiumRepository

    override fun onCreate() {
        super.onCreate()

        // 1. Ambil WebService dari ApiConfig buatan timmu
        val retrofitService = ApiConfig.webService

        // 2. Rakit Repository khusus untuk fitur Premium/AI milikmu
        premiumRepository = PremiumRepositoryImpl(
            remoteDataSource = PremiumRemoteDataSourceImpl(retrofitService),
            journalDao = AppDatabase.getDatabase(this).journalDao()
        )
    }
}