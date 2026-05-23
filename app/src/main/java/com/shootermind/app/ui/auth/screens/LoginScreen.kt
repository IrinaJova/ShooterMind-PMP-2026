package com.shootermind.app.ui.auth.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.ui.auth.AuthUiState
import com.shootermind.app.ui.auth.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Navigate away once auth succeeds
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.clearState()
            onLoginSuccess()
        }
    }

    val isLoading = uiState is AuthUiState.Loading
    val errorMsg  = (uiState as? AuthUiState.Error)?.message

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.app_name),
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = stringResource(R.string.screen_login),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(36.dp))

            // ── Error banner ─────────────────────────────────────────────────
            if (errorMsg != null) {
                Text(
                    text     = errorMsg,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            // ── Fields ───────────────────────────────────────────────────────
            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it; authViewModel.clearState() },
                label           = { Text(stringResource(R.string.label_email)) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled         = !isLoading,
                modifier        = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it; authViewModel.clearState() },
                label                = { Text(stringResource(R.string.label_password)) },
                singleLine           = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled              = !isLoading,
                modifier             = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // ── Primary action ───────────────────────────────────────────────
            Button(
                onClick  = { authViewModel.signInWithEmail(email.trim(), password) },
                enabled  = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.btn_login))
                }
            }
            Spacer(Modifier.height(24.dp))

            // ── Divider ──────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text     = stringResource(R.string.login_or_divider),
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            // ── Social / guest ───────────────────────────────────────────────
            OutlinedButton(
                onClick  = { /* TODO Phase 3 – Google Sign-In */ },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_sign_in_google))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = { /* TODO Phase 3 – Facebook Sign-In */ },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_sign_in_facebook))
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick  = { authViewModel.signInAnonymously() },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_continue_anonymously))
            }
            Spacer(Modifier.height(24.dp))

            // ── Register link ────────────────────────────────────────────────
            TextButton(
                onClick  = onNavigateToRegister,
                enabled  = !isLoading
            ) {
                Text(
                    text      = stringResource(R.string.login_no_account),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
