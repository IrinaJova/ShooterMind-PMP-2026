package com.shootermind.app.data.repository

import android.content.Context
import com.shootermind.app.domain.model.AuthUser

interface AuthRepository {
    val isLoggedIn: Boolean
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<AuthUser>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signInWithGoogle(context: Context): Result<AuthUser>
    fun signOut()
}
