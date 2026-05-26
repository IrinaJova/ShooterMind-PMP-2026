package com.shootermind.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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

    // ── Password reset ─────────────────────────────────────────────────────

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    // ── Delete account ─────────────────────────────────────────────────────
    // For email/password accounts [password] is required for re-authentication.
    // For Google/Facebook accounts pass null — Firebase allows deletion when the
    // session is recent enough; otherwise it throws FirebaseAuthRecentLoginRequiredException.

    override suspend fun deleteAccount(password: String?): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No user signed in")
        val uid  = user.uid

        // Re-authenticate email/password users
        if (password != null) {
            val email      = user.email ?: error("No email associated with account")
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
        }

        // Delete Firestore data (best effort — don't let this block auth deletion)
        try {
            val db       = Firebase.firestore
            val sessions = db.collection("users").document(uid)
                .collection("sessions").get().await()
            for (doc in sessions.documents) { doc.reference.delete().await() }
            db.collection("users").document(uid).delete().await()
        } catch (_: Exception) { /* continue even if Firestore cleanup fails */ }

        // Delete the Firebase Auth account
        user.delete().await()
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
