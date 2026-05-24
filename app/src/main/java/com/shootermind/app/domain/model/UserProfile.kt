package com.shootermind.app.domain.model

data class UserProfile(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val birthDateMs: Long,
    val issfCategory: ISSFCategory,
    val discipline: Discipline,
    val personalBest: Double,
    val goal: TrainingGoal,
    val profilePictureUri: String?
)
