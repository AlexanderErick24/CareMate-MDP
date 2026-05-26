package com.example.caremate.data.sources.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    indices = [
        Index(value = ["intake_hour", "intake_minute"]),
        Index(value = ["is_enabled"])
    ]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "dosage")
    val dosage: String,
    @ColumnInfo(name = "intake_hour")
    val intakeHour: Int,
    @ColumnInfo(name = "intake_minute")
    val intakeMinute: Int,
    @ColumnInfo(name = "is_taken_today")
    val isTakenToday: Boolean = false,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
