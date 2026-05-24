package com.shootermind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val discipline: String,   // Discipline.name — "AIR_RIFLE" | "AIR_PISTOL"
    val dateMs: Long,
    val totalScore: Double,
    val shotCount: Int,
    val notes: String
)
