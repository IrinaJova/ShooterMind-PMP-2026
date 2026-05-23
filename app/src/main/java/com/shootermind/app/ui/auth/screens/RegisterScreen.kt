package com.shootermind.app.ui.auth.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.ui.auth.AuthUiState
import com.shootermind.app.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var displayName     by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError      by remember { mutableStateOf<String?>(null) }

    // Navigate away once auth succeeds
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.clearState()
            onRegisterSuccess()
        }
    }

    val isLoading   = uiState is AuthUiState.Loading
    val remoteError = (uiState as? AuthUiState.Error)?.message
    val shownError  = localError ?: remoteError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_register)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Error banner ─────────────────────────────────────────────
                if (shownError != null) {
                    Text(
                        text     = shownError,
                        color    = MaterialTheme.colorScheme.error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // ── Fields ───────────────────────────────────────────────────
                OutlinedTextField(
                    value         = displayName,
                    onValueChange = { displayName = it; localError = null; authViewModel.clearState() },
                    label         = { Text(stringResource(R.string.label_display_name)) },
                    singleLine    = true,
                    enabled       = !isLoading,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it; localError = null; authViewModel.clearState() },
                    label           = { Text(stringResource(R.string.label_email)) },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled         = !isLoading,
                    modifier        = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; localError = null; authViewModel.clearState() },
                    label                = { Text(stringResource(R.string.label_password)) },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled              = !isLoading,
                    modifier             = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value                = confirmPassword,
                    onValueChange        = { confirmPassword = it; localError = null },
                    label                = { Text(stringResource(R.string.label_confirm_password)) },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError              = confirmPassword.isNotBlank() && confirmPassword != password,
                    enabled              = !isLoading,
                    modifier             = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(28.dp))

                // ── Register button ──────────────────────────────────────────
                val errRequired   = stringResource(R.string.error_field_required)
                val errEmailEmpty = stringResource(R.string.error_email_empty)
                val errShortPw    = stringResource(R.string.error_password_too_short)
                val errNoMatch    = stringResource(R.string.error_passwords_no_match)

                Button(
                    onClick = {
                        when {
                            displayName.isBlank()      -> localError = errRequired
                            email.isBlank()            -> localError = errEmailEmpty
                            password.length < 6        -> localError = errShortPw
                            password != confirmPassword -> localError = errNoMatch
                            else -> authViewModel.registerWithEmail(
                                email.trim(), password, displayName.trim()
                            )
                        }
                    },
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.btn_register))
                    }
                }
                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick  = onNavigateToLogin,
                    enabled  = !isLoading
                ) {
                    Text(
                        text      = stringResource(R.string.register_have_account),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
