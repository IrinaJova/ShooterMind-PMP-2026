package com.shootermind.app.data.repository

import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(userId: String): Flow<List<TrainingSession>>
    fun getRecentSessions(userId: String, limit: Int = 5): Flow<List<TrainingSession>>
    suspend fun insertSession(session: TrainingSession)
    suspend fun deleteSession(session: TrainingSession)
}
