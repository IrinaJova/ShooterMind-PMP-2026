package com.shootermind.app.data.repository

import com.shootermind.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getProfile(userId: String): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
}
