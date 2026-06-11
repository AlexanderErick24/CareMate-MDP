package com.mdp.caremate.data.sources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationHistoryDao {
    @Insert
    suspend fun insertHistory(history: MedicationHistoryEntity)

    @Query("DELETE FROM medication_history WHERE medicationId = :medicationId AND takenAt >= :startOfDay")
    suspend fun deleteTodayHistory(medicationId: Long, startOfDay: Long)

    @Query("SELECT * FROM medication_history ORDER BY takenAt DESC")
    fun observeHistory(): Flow<List<MedicationHistoryEntity>>
}
