package com.shootermind.app.ui.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shootermind.app.R
import com.shootermind.app.core.util.LocaleUtils
import com.shootermind.app.core.util.OnboardingPrefs
import com.shootermind.app.domain.model.ThemeMode
import com.shootermind.app.ui.auth.AuthUiState
import com.shootermind.app.ui.auth.AuthViewModel
import com.shootermind.app.ui.theme.ThemeViewModel

private val Purple500 = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel  : ThemeViewModel,
    onNavigateBack  : () -> Unit,
    onEditProfile   : () -> Unit = {},
    onSignOut       : () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    authViewModel   : AuthViewModel = viewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val context    = LocalContext.current
    var isMk       by remember { mutableStateOf(LocaleUtils.isMacedonian()) }

    // Is the current user an email/password account?
    val isEmailUser = remember {
        Firebase.auth.currentUser?.providerData
            ?.any { it.providerId == "password" } == true
    }
    val currentEmail = remember { Firebase.auth.currentUser?.email ?: "" }

    // Dialog states
    var showResetDialog         by remember { mutableStateOf(false) }
    var showResetSuccessDialog  by remember { mutableStateOf(false) }
    var showDeleteDialog        by remember { mutableStateOf(false) }
    var deletePasswordInput     by remember { mutableStateOf("") }
    var dialogError             by remember { mutableStateOf<String?>(null) }

    val isWorking = authState is AuthUiState.Loading

    // ── Observe auth state changes ────────────────────────────────────────
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.PasswordResetSent -> {
                showResetDialog        = false
                showResetSuccessDialog = true
                authViewModel.clearState()
            }
            is AuthUiState.AccountDeleted -> {
                showDeleteDialog = false
                authViewModel.clearState()
                onAccountDeleted()
            }
            is AuthUiState.Error -> {
                dialogError = (authState as AuthUiState.Error).message
            }
            else -> {
                if (authState !is AuthUiState.Loading) dialogError = null
            }
        }
    }

    // ── Reset password confirm dialog ─────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!isWorking) { showResetDialog = false; authViewModel.clearState(); dialogError = null } },
            title   = { Text(stringResource(R.string.settings_reset_password)) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_reset_password_desc))
                    if (currentEmail.isNotBlank()) {
                        Text(
                            text  = currentEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (dialogError != null) {
                        Text(
                            text  = dialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = { authViewModel.sendPasswordReset(currentEmail) },
                    enabled  = !isWorking
                ) {
                    if (isWorking) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick  = { showResetDialog = false; authViewModel.clearState(); dialogError = null },
                    enabled  = !isWorking
                ) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    // ── Reset password success dialog ─────────────────────────────────────
    if (showResetSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showResetSuccessDialog = false },
            title   = { Text(stringResource(R.string.settings_reset_password)) },
            text    = {
                Text(
                    text  = stringResource(R.string.settings_reset_password_sent),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            confirmButton = {
                Button(onClick = { showResetSuccessDialog = false }) { Text("OK") }
            }
        )
    }

    // ── Delete account dialog ─────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isWorking) {
                    showDeleteDialog    = false
                    deletePasswordInput = ""
                    dialogError         = null
                    authViewModel.clearState()
                }
            },
            title = { Text(stringResource(R.string.settings_delete_account)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text  = stringResource(R.string.settings_delete_account_warning),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Only email/password users need to re-authenticate
                    if (isEmailUser) {
                        OutlinedTextField(
                            value                = deletePasswordInput,
                            onValueChange        = { deletePasswordInput = it; dialogError = null },
                            label                = { Text(stringResource(R.string.settings_enter_password_to_confirm)) },
                            singleLine           = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled              = !isWorking,
                            modifier             = Modifier.fillMaxWidth()
                        )
                    }
                    if (dialogError != null) {
                        Text(
                            text  = dialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = {
                        val pwd = if (isEmailUser) deletePasswordInput else null
                        authViewModel.deleteAccount(pwd)
                    },
                    enabled  = !isWorking && (!isEmailUser || deletePasswordInput.isNotBlank()),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isWorking) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.settings_delete_confirm_button))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick  = {
                        showDeleteDialog    = false
                        deletePasswordInput = ""
                        dialogError         = null
                        authViewModel.clearState()
                    },
                    enabled  = !isWorking
                ) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    // ── Main scaffold ─────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── ACCOUNT ───────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_account)) {
                SettingsRow(
                    icon    = Icons.Default.Person,
                    label   = stringResource(R.string.settings_edit_profile),
                    onClick = onEditProfile
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                // Show Reset Password only for email/password accounts
                if (isEmailUser) {
                    SettingsRow(
                        icon    = Icons.Default.Lock,
                        label   = stringResource(R.string.settings_reset_password),
                        onClick = {
                            dialogError     = null
                            showResetDialog = true
                            authViewModel.clearState()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                SettingsRow(
                    icon       = Icons.Default.Logout,
                    label      = stringResource(R.string.settings_sign_out),
                    labelColor = MaterialTheme.colorScheme.error,
                    iconTint   = MaterialTheme.colorScheme.error,
                    onClick    = onSignOut
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    icon       = Icons.Default.DeleteForever,
                    label      = stringResource(R.string.settings_delete_account),
                    labelColor = MaterialTheme.colorScheme.error,
                    iconTint   = MaterialTheme.colorScheme.error,
                    onClick    = {
                        deletePasswordInput = ""
                        dialogError         = null
                        showDeleteDialog    = true
                        authViewModel.clearState()
                    }
                )
            }

            // ── APPEARANCE ────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_appearance)) {
                // Language
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text       = stringResource(R.string.settings_language),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageButton(
                            label    = stringResource(R.string.settings_language_english),
                            selected = !isMk,
                            modifier = Modifier.weight(1f),
                            onClick  = {
                                if (isMk) {
                                    isMk = false
                                    OnboardingPrefs.setLanguage(context, "en")
                                    LocaleUtils.setLocale("en")
                                    (context as? Activity)?.recreate()
                                }
                            }
                        )
                        LanguageButton(
                            label    = stringResource(R.string.settings_language_macedonian),
                            selected = isMk,
                            modifier = Modifier.weight(1f),
                            onClick  = {
                                if (!isMk) {
                                    isMk = true
                                    OnboardingPrefs.setLanguage(context, "mk")
                                    LocaleUtils.setLocale("mk")
                                    (context as? Activity)?.recreate()
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(R.string.settings_language_restart_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Theme
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text  = stringResource(R.string.settings_theme),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeToggleButton(
                            label    = stringResource(R.string.settings_theme_light),
                            selected = themeMode == ThemeMode.LIGHT,
                            modifier = Modifier.weight(1f),
                            onClick  = { themeViewModel.setTheme(ThemeMode.LIGHT) }
                        )
                        ThemeToggleButton(
                            label    = stringResource(R.string.settings_theme_system),
                            selected = themeMode == ThemeMode.SYSTEM,
                            modifier = Modifier.weight(1f),
                            onClick  = { themeViewModel.setTheme(ThemeMode.SYSTEM) }
                        )
                        ThemeToggleButton(
                            label    = stringResource(R.string.settings_theme_dark),
                            selected = themeMode == ThemeMode.DARK,
                            modifier = Modifier.weight(1f),
                            onClick  = { themeViewModel.setTheme(ThemeMode.DARK) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = stringResource(R.string.settings_theme_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── NOTIFICATIONS ─────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_notifications)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = stringResource(R.string.settings_notif_enabled),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text  = stringResource(R.string.settings_notif_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = true, onCheckedChange = {})
                    }
                }
            }

            // ── ABOUT ─────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_about)) {
                SettingsInfoRow(
                    icon  = Icons.Default.Info,
                    label = stringResource(R.string.settings_version),
                    value = stringResource(R.string.settings_version_value)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoRow(
                    icon  = Icons.Default.AccountCircle,
                    label = "ShooterMind",
                    value = "© 2026"
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Section container ────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title  : String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text       = title.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            content()
        }
    }
}

// ── Clickable row ────────────────────────────────────────────────────────────

@Composable
private fun SettingsRow(
    icon      : ImageVector,
    label     : String,
    onClick   : () -> Unit,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint  : Color = Purple500
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            color      = labelColor,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Purple500, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.weight(1f)
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Language button ──────────────────────────────────────────────────────────

@Composable
private fun LanguageButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) FilledTonalButton(onClick = {}, modifier = modifier) { Text(label) }
    else OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
}

// ── Theme toggle ─────────────────────────────────────────────────────────────

@Composable
private fun ThemeToggleButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        FilledTonalButton(onClick = {}, modifier = modifier) {
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
