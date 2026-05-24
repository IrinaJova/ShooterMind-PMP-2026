package com.shootermind.app.data.repository

import com.shootermind.app.data.local.dao.UserProfileDao
import com.shootermind.app.data.local.entity.UserProfileEntity
import com.shootermind.app.data.remote.FirestoreSyncService
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.ISSFCategory
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl(
    private val dao   : UserProfileDao,
    private val remote: FirestoreSyncService = FirestoreSyncService()
) : UserProfileRepository {

    override fun getProfile(userId: String): Flow<UserProfile?> =
        dao.getProfile(userId).map { it?.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) {
        dao.upsert(profile.toEntity())
        runCatching { remote.upsertProfile(profile) }
    }

    /** Pull profile from Firestore into Room (called on login). */
    suspend fun syncFromCloud(userId: String) {
        runCatching {
            val cloudProfile = remote.fetchProfile(userId) ?: return
            dao.upsert(cloudProfile.toEntity())
        }
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private fun UserProfileEntity.toDomain() = UserProfile(
        userId            = userId,
        firstName         = firstName,
        lastName          = lastName,
        birthDateMs       = birthDateMs,
        issfCategory      = ISSFCategory.valueOf(issfCategory),
        discipline        = Discipline.valueOf(discipline),
        personalBest      = personalBest,
        goal              = TrainingGoal.valueOf(goal),
        profilePictureUri = profilePictureUri
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        userId            = userId,
        firstName         = firstName,
        lastName          = lastName,
        birthDateMs       = birthDateMs,
        issfCategory      = issfCategory.name,
        discipline        = discipline.name,
        personalBest      = personalBest,
        goal              = goal.name,
        profilePictureUri = profilePictureUri
    )
}
