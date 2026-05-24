package com.shootermind.app.domain.model

data class TrainingSession(
    val id: String,
    val userId: String,
    val discipline: Discipline,
    val dateMs: Long,
    val totalScore: Double,
    val shotCount: Int,
    val notes: String
)
