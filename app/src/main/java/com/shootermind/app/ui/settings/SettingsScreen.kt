package com.shootermind.app.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shootermind.app.R
import com.shootermind.app.core.util.LocaleUtils
import com.shootermind.app.core.util.OnboardingPrefs
import com.shootermind.app.domain.model.ThemeMode
import com.shootermind.app.ui.theme.ThemeViewModel

private val Purple700 = Color(0xFF6D28D9)
private val Purple500 = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel    : ThemeViewModel,
    onNavigateBack    : () -> Unit,
    onEditProfile     : () -> Unit = {},
    onSignOut         : () -> Unit = {}
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val context   = LocalContext.current
    var isMk      by remember { mutableStateOf(LocaleUtils.isMacedonian()) }

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
                SettingsRow(
                    icon       = Icons.Default.Logout,
                    label      = stringResource(R.string.settings_sign_out),
                    labelColor = MaterialTheme.colorScheme.error,
                    iconTint   = MaterialTheme.colorScheme.error,
                    onClick    = onSignOut
                )
            }

            // ── APPEARANCE ────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_appearance)) {
                // Language row
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
                            text  = stringResource(R.string.settings_language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
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

                // Theme row
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
                        // Placeholder switch — real notification pref can be added later
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
        Icon(
            icon,
            contentDescription = null,
            tint     = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = labelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
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
private fun SettingsInfoRow(
    icon : ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Purple500, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
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
private fun LanguageButton(
    label   : String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick : () -> Unit
) {
    if (selected) {
        FilledTonalButton(onClick = {}, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}

// ── Theme toggle ─────────────────────────────────────────────────────────────

@Composable
private fun ThemeToggleButton(
    label   : String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick : () -> Unit
) {
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
