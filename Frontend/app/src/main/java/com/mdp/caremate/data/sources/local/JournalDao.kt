package com.mdp.caremate.data.sources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity)

    @Query("SELECT * FROM journal_table ORDER BY timestamp DESC")
    suspend fun getAllJournals(): List<JournalEntity>
}