package com.example.caremate.data.sources.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteMedicationById(medicationId: Long)

    @Query("SELECT * FROM medications ORDER BY intake_hour ASC, intake_minute ASC")
    fun observeAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE is_enabled = 1 ORDER BY intake_hour ASC, intake_minute ASC")
    fun observeTodaysMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :medicationId LIMIT 1")
    suspend fun getMedicationById(medicationId: Long): MedicationEntity?

    @Query(
        "UPDATE medications " +
            "SET is_taken_today = :isTakenToday, updated_at = :updatedAt " +
            "WHERE id = :medicationId"
    )
    suspend fun updateTakenStatus(
        medicationId: Long,
        isTakenToday: Boolean,
        updatedAt: Long
    )

    @Query(
        "UPDATE medications " +
            "SET is_taken_today = 0, updated_at = :updatedAt"
    )
    suspend fun resetTakenStatus(updatedAt: Long)
}
