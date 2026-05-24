package com.shootermind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val firstName: String,
    val lastName: String,
    val birthDateMs: Long,
    val issfCategory: String,   // ISSFCategory.name
    val discipline: String,     // Discipline.name
    val personalBest: Double,
    val goal: String,           // TrainingGoal.name
    val profilePictureUri: String?
)
