package com.shootermind.app.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shootermind.app.core.analytics.AnalyticsHelper
import com.shootermind.app.data.repository.AuthRepository
import com.shootermind.app.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository = AuthRepositoryImpl()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn : Boolean get() = repository.isLoggedIn
    val isAnonymous: Boolean get() = Firebase.auth.currentUser?.isAnonymous == true

    // ── Email / password sign-in ───────────────────────────────────────────
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signInWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = {
                    AnalyticsHelper.logLogin("email")
                    AuthUiState.Success(it)
                },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Email / password registration ──────────────────────────────────────
    fun registerWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.registerWithEmail(email, password, displayName)
            _uiState.value = result.fold(
                onSuccess = {
                    AnalyticsHelper.logSignUp("email")
                    AuthUiState.Success(it)
                },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Anonymous sign-in ──────────────────────────────────────────────────
    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signInAnonymously()
            _uiState.value = result.fold(
                onSuccess = {
                    AnalyticsHelper.logLogin("anonymous")
                    AuthUiState.Success(it)
                },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signInWithGoogle(context)
            _uiState.value = result.fold(
                onSuccess = {
                    AnalyticsHelper.logLogin("google")
                    AuthUiState.Success(it)
                },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Facebook Sign-In ───────────────────────────────────────────────────
    fun handleFacebookToken(token: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.handleFacebookToken(token)
            _uiState.value = result.fold(
                onSuccess = {
                    AnalyticsHelper.logLogin("facebook")
                    AuthUiState.Success(it)
                },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Password reset (forgot password / settings reset) ─────────────────
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.sendPasswordResetEmail(email.trim())
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.PasswordResetSent },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Delete account ─────────────────────────────────────────────────────
    // [password] must be supplied for email/password accounts (re-authentication).
    // Pass null for Google / Facebook / anonymous accounts.
    fun deleteAccount(password: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.deleteAccount(password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.AccountDeleted },
                onFailure = { AuthUiState.Error(friendlyAuthError(it)) }
            )
        }
    }

    // ── Sign-out ───────────────────────────────────────────────────────────
    fun signOut() {
        AnalyticsHelper.logSignOut()
        repository.signOut()
        _uiState.value = AuthUiState.Idle
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }

    /**
     * Converts raw Firebase exception messages into short, user-friendly strings.
     * Firebase (since late 2023) intentionally merges "wrong password" and
     * "user not found" into one generic credential error to prevent enumeration.
     */
    private fun friendlyAuthError(e: Throwable): String {
        val msg = e.message?.lowercase() ?: return "Authentication failed. Please try again."
        return when {
            // Wrong email or password (Firebase consolidated error)
            msg.contains("credential") ||
            msg.contains("malformed")  ||
            msg.contains("incorrect")  ||
            msg.contains("invalid login") ||
            msg.contains("wrong password") ->
                "Incorrect email or password."

            // Account not found
            msg.contains("no user")       ||
            msg.contains("user not found") ->
                "No account found with this email."

            // Email format
            msg.contains("invalid email") ||
            msg.contains("badly formatted") ->
                "Please enter a valid email address."

            // Account disabled
            msg.contains("disabled") ->
                "This account has been disabled. Contact support."

            // Too many attempts
            msg.contains("too many") ||
            msg.contains("quota")    ||
            msg.contains("blocked")  ->
                "Too many failed attempts. Please try again later."

            // Network
            msg.contains("network")     ||
            msg.contains("connection")  ||
            msg.contains("unavailable") ->
                "No internet connection. Please check your network."

            // Weak password (registration)
            msg.contains("weak password") ||
            msg.contains("at least 6") ->
                "Password must be at least 6 characters."

            // Email already in use (registration)
            msg.contains("already in use") ||
            msg.contains("email address is already") ->
                "An account with this email already exists."

            // Recent login required (delete account)
            msg.contains("recent login") ||
            msg.contains("requires recent") ->
                "Please sign out and sign in again before deleting your account."

            else -> "Authentication failed. Please try again."
        }
    }
}
