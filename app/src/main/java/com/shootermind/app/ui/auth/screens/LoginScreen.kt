package com.shootermind.app.ui.auth.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.shootermind.app.R
import com.shootermind.app.ui.auth.AuthUiState
import com.shootermind.app.ui.auth.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess      : () -> Unit,
    authViewModel       : AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context  = LocalContext.current

    // ── Facebook setup ────────────────────────────────────────────────────
    val callbackManager = remember { CallbackManager.Factory.create() }
    val facebookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        callbackManager.onActivityResult(result.resultCode, result.resultCode, result.data)
    }
    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    authViewModel.handleFacebookToken(result.accessToken.token)
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException) {
                    authViewModel.handleFacebookToken("")
                }
            }
        )
        onDispose { LoginManager.getInstance().unregisterCallback(callbackManager) }
    }

    // ── Local state ───────────────────────────────────────────────────────
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var failedAttempts  by remember { mutableIntStateOf(0) }
    var loginAttempted  by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail     by remember { mutableStateOf("") }

    val isLoading = uiState is AuthUiState.Loading
    val errorMsg  = (uiState as? AuthUiState.Error)?.message

    // ── Effects ───────────────────────────────────────────────────────────
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                authViewModel.clearState()
                onLoginSuccess()
            }
            is AuthUiState.Error -> {
                // Only count failures caused by a login attempt
                if (loginAttempted) {
                    failedAttempts++
                    loginAttempted = false
                }
            }
            is AuthUiState.PasswordResetSent -> {
                showForgotDialog = false
                authViewModel.clearState()
            }
            else -> {}
        }
    }

    // ── Forgot password dialog ────────────────────────────────────────────
    if (showForgotDialog) {
        val resetState       = uiState
        val isResetting      = resetState is AuthUiState.Loading
        val resetSent        = resetState is AuthUiState.PasswordResetSent
        val resetError       = (resetState as? AuthUiState.Error)?.message

        AlertDialog(
            onDismissRequest = {
                if (!isResetting) {
                    showForgotDialog = false
                    authViewModel.clearState()
                }
            },
            title   = { Text(stringResource(R.string.forgot_password_title)) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.forgot_password_body))
                    OutlinedTextField(
                        value           = forgotEmail,
                        onValueChange   = { forgotEmail = it; authViewModel.clearState() },
                        label           = { Text(stringResource(R.string.label_email)) },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled         = !isResetting,
                        modifier        = Modifier.fillMaxWidth()
                    )
                    if (resetError != null) {
                        Text(
                            text  = resetError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (resetSent) {
                        Text(
                            text  = stringResource(R.string.forgot_password_sent),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                if (!resetSent) {
                    Button(
                        onClick  = { authViewModel.sendPasswordReset(forgotEmail) },
                        enabled  = !isResetting && forgotEmail.isNotBlank()
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.btn_save))
                        }
                    }
                } else {
                    Button(onClick = {
                        showForgotDialog = false
                        authViewModel.clearState()
                    }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                if (!resetSent) {
                    TextButton(
                        onClick  = { showForgotDialog = false; authViewModel.clearState() },
                        enabled  = !isResetting
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            }
        )
    }

    // ── Main UI ───────────────────────────────────────────────────────────
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
            Spacer(Modifier.height(8.dp))

            // ── Forgot password link ─────────────────────────────────────────
            // Always visible; becomes more prominent after 2+ failed attempts
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(
                    onClick  = {
                        forgotEmail = email   // pre-fill with whatever was typed
                        showForgotDialog = true
                        authViewModel.clearState()
                    },
                    enabled  = !isLoading
                ) {
                    Text(
                        text       = stringResource(R.string.forgot_password),
                        style      = MaterialTheme.typography.bodySmall,
                        color      = if (failedAttempts >= 2)
                                         MaterialTheme.colorScheme.error
                                     else
                                         MaterialTheme.colorScheme.primary,
                        fontWeight = if (failedAttempts >= 2) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Primary action ───────────────────────────────────────────────
            Button(
                onClick  = {
                    loginAttempted = true
                    authViewModel.signInWithEmail(email.trim(), password)
                },
                enabled  = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
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
                onClick  = { authViewModel.signInWithGoogle(context) },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_sign_in_google))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = {
                    LoginManager.getInstance().logInWithReadPermissions(
                        context as androidx.activity.ComponentActivity,
                        callbackManager,
                        listOf("email", "public_profile")
                    )
                },
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
