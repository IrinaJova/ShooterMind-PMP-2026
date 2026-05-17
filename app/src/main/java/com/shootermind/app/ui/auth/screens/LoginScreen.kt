package com.shootermind.app.ui.auth.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shootermind.app.R

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
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

            // ── Fields ───────────────────────────────────────────────────────
            OutlinedTextField(
                value           = "",
                onValueChange   = {},
                label           = { Text(stringResource(R.string.label_email)) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier        = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value                = "",
                onValueChange        = {},
                label                = { Text(stringResource(R.string.label_password)) },
                singleLine           = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier             = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // ── Primary action ───────────────────────────────────────────────
            Button(
                onClick  = onLoginSuccess, // Phase 2 – Firebase Auth
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_login))
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
                onClick  = { /* Phase 2 – Google Sign-In */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_sign_in_google))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = { /* Phase 2 – Facebook Sign-In */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_sign_in_facebook))
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick  = onLoginSuccess, // Phase 2 – anonymous auth
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_continue_anonymously))
            }
            Spacer(Modifier.height(24.dp))

            // ── Register link ────────────────────────────────────────────────
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text      = stringResource(R.string.login_no_account),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
