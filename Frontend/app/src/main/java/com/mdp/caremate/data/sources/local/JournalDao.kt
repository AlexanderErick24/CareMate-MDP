package com.mdp.caremate.data.sources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity)

    @Query("SELECT * FROM journal_table WHERE caregiverId = :caregiverId ORDER BY timestamp ASC")
    suspend fun getJournalsByCaregiver(caregiverId: String): List<JournalEntity>
}