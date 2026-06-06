package com.mdp.caremate.data.sources.local

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MedicationDailyResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        AppDatabase.getDatabase(applicationContext)
            .medicationDao()
            .resetTakenStatus(System.currentTimeMillis())
        return Result.success()
    }
}
