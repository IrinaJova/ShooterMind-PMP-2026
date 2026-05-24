package com.shootermind.app.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    val isLoggedIn: Boolean get() = repository.isLoggedIn

    // ── Email / password sign-in ───────────────────────────────────────────
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signInWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // ── Email / password registration ──────────────────────────────────────
    fun registerWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.registerWithEmail(email, password, displayName)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // ── Anonymous sign-in ──────────────────────────────────────────────────
    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signInAnonymously()
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.signInWithGoogle(context)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Google sign-in failed") }
            )
        }
    }

    // ── Facebook Sign-In ───────────────────────────────────────────────────
    fun handleFacebookToken(token: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.handleFacebookToken(token)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Facebook sign-in failed") }
            )
        }
    }

    // ── Sign-out ───────────────────────────────────────────────────────────
    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState.Idle
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}
