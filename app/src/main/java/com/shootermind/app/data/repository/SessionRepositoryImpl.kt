package com.shootermind.app.data.repository

import com.shootermind.app.data.local.dao.TrainingSessionDao
import com.shootermind.app.data.local.entity.TrainingSessionEntity
import com.shootermind.app.data.remote.FirestoreSyncService
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    private val dao   : TrainingSessionDao,
    private val remote: FirestoreSyncService = FirestoreSyncService()
) : SessionRepository {

    override fun getAllSessions(userId: String): Flow<List<TrainingSession>> =
        dao.getAllByUser(userId).map { list -> list.map { it.toDomain() } }

    override fun getRecentSessions(userId: String, limit: Int): Flow<List<TrainingSession>> =
        dao.getRecentByUser(userId, limit).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSession(session: TrainingSession) {
        dao.insert(session.toEntity())
        runCatching { remote.upsertSession(session) } // best-effort, never crash offline
    }

    override suspend fun deleteSession(session: TrainingSession) {
        dao.delete(session.toEntity())
        runCatching { remote.deleteSession(session) }
    }

    /** Pull cloud sessions into local Room (called on login). */
    suspend fun syncFromCloud(userId: String) {
        runCatching {
            val cloudSessions = remote.fetchSessions(userId)
            cloudSessions.forEach { dao.insert(it.toEntity()) }
        }
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private fun TrainingSessionEntity.toDomain() = TrainingSession(
        id               = id,
        userId           = userId,
        discipline       = Discipline.valueOf(discipline),
        dateMs           = dateMs,
        totalScore       = totalScore,
        shotCount        = shotCount,
        notes            = notes,
        seriesData       = seriesData,
        durationMinutes  = durationMinutes,
        photoUri         = photoUri,
        learnedToday     = learnedToday,
        mistakesMade     = mistakesMade,
        improvements     = improvements,
        startHour        = startHour,
        startMinute      = startMinute,
        endHour          = endHour,
        endMinute        = endMinute,
        isCompetition    = isCompetition,
        isControlSession = isControlSession,
        useDecimalScore  = useDecimalScore,
        splitIntoSeries  = splitIntoSeries,
        seriesCount      = seriesCount,
        shotsPerSeries   = shotsPerSeries,
        batch            = batch,
        airPressure      = airPressure,
        muscleRecovery   = muscleRecovery,
        fatigue          = fatigue,
        concentration    = concentration,
        endurance        = endurance,
        heartRate        = heartRate
    )

    private fun TrainingSession.toEntity() = TrainingSessionEntity(
        id               = id,
        userId           = userId,
        discipline       = discipline.name,
        dateMs           = dateMs,
        totalScore       = totalScore,
        shotCount        = shotCount,
        notes            = notes,
        seriesData       = seriesData,
        durationMinutes  = durationMinutes,
        photoUri         = photoUri,
        learnedToday     = learnedToday,
        mistakesMade     = mistakesMade,
        improvements     = improvements,
        startHour        = startHour,
        startMinute      = startMinute,
        endHour          = endHour,
        endMinute        = endMinute,
        isCompetition    = isCompetition,
        isControlSession = isControlSession,
        useDecimalScore  = useDecimalScore,
        splitIntoSeries  = splitIntoSeries,
        seriesCount      = seriesCount,
        shotsPerSeries   = shotsPerSeries,
        batch            = batch,
        airPressure      = airPressure,
        muscleRecovery   = muscleRecovery,
        fatigue          = fatigue,
        concentration    = concentration,
        endurance        = endurance,
        heartRate        = heartRate
    )
}
