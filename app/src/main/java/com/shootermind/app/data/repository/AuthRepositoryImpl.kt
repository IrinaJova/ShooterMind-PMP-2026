package com.shootermind.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.shootermind.app.domain.model.AuthUser
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = Firebase.auth

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // ── Email / password ───────────────────────────────────────────────────

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
            user.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
            ).await()
        }
        user.toAuthUser().copy(displayName = displayName.ifBlank { null })
    }

    // ── Anonymous ──────────────────────────────────────────────────────────

    override suspend fun signInAnonymously(): Result<AuthUser> = runCatching {
        val result = auth.signInAnonymously().await()
        result.user!!.toAuthUser()
    }

    // ── Google Sign-In via Credential Manager ──────────────────────────────

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> = runCatching {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialResult = credentialManager.getCredential(context, request)
        val credential = credentialResult.credential

        check(
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Unexpected credential type: ${credential.type}" }

        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        authResult.user!!.toAuthUser()
    }

    // ── Facebook Sign-In ───────────────────────────────────────────────────

    override suspend fun handleFacebookToken(token: String): Result<AuthUser> = runCatching {
        val credential = FacebookAuthProvider.getCredential(token)
        val authResult = auth.signInWithCredential(credential).await()
        authResult.user!!.toAuthUser()
    }

    // ── Sign-out ───────────────────────────────────────────────────────────

    override fun signOut() {
        auth.signOut()
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid         = uid,
        email       = email,
        displayName = displayName,
        isAnonymous = isAnonymous
    )

    companion object {
        // Web client (type 3) from google-services.json
        const val WEB_CLIENT_ID =
            "394347539737-2sv850hofie6p5ll7h6afsa2ft0labql.apps.googleusercontent.com"
    }
}
