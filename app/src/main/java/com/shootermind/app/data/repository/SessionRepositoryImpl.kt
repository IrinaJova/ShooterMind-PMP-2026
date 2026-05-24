package com.shootermind.app.data.repository

import com.shootermind.app.data.local.dao.TrainingSessionDao
import com.shootermind.app.data.local.entity.TrainingSessionEntity
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    private val dao: TrainingSessionDao
) : SessionRepository {

    override fun getAllSessions(userId: String): Flow<List<TrainingSession>> =
        dao.getAllByUser(userId).map { list -> list.map { it.toDomain() } }

    override fun getRecentSessions(userId: String, limit: Int): Flow<List<TrainingSession>> =
        dao.getRecentByUser(userId, limit).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSession(session: TrainingSession) =
        dao.insert(session.toEntity())

    override suspend fun deleteSession(session: TrainingSession) =
        dao.delete(session.toEntity())

    // ── Mapping helpers ────────────────────────────────────────────────────

    private fun TrainingSessionEntity.toDomain() = TrainingSession(
        id         = id,
        userId     = userId,
        discipline = Discipline.valueOf(discipline),
        dateMs     = dateMs,
        totalScore = totalScore,
        shotCount  = shotCount,
        notes      = notes
    )

    private fun TrainingSession.toEntity() = TrainingSessionEntity(
        id         = id,
        userId     = userId,
        discipline = discipline.name,
        dateMs     = dateMs,
        totalScore = totalScore,
        shotCount  = shotCount,
        notes      = notes
    )
}
