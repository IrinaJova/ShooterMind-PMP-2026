package com.shootermind.app.ui.auth

import com.shootermind.app.domain.model.AuthUser

sealed interface AuthUiState {
    data object Idle              : AuthUiState
    data object Loading           : AuthUiState
    data class  Success(val user: AuthUser) : AuthUiState
    data class  Error(val message: String)  : AuthUiState
    data object PasswordResetSent : AuthUiState
    data object AccountDeleted    : AuthUiState
}
