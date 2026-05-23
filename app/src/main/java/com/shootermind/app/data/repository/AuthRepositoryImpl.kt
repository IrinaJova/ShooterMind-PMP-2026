package com.shootermind.app.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.shootermind.app.domain.model.AuthUser
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = Firebase.auth

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user!!.toAuthUser()
        }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<AuthUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user!!
        if (displayName.isNotBlank()) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
        }
        user.toAuthUser().copy(displayName = displayName.ifBlank { null })
    }

    override suspend fun signInAnonymously(): Result<AuthUser> = runCatching {
        val result = auth.signInAnonymously().await()
        result.user!!.toAuthUser()
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid         = uid,
        email       = email,
        displayName = displayName,
        isAnonymous = isAnonymous
    )
}
