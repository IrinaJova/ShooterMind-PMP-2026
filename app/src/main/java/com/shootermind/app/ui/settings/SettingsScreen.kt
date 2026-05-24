package com.shootermind.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shootermind.app.R
import com.shootermind.app.core.util.LocaleUtils
import com.shootermind.app.domain.model.ThemeMode
import com.shootermind.app.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    onNavigateBack: () -> Unit
) {
    val themeMode  by themeViewModel.themeMode.collectAsState()
    var isMacedonian by remember { mutableStateOf(LocaleUtils.isMacedonian()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(24.dp)
        ) {

            // ── Language ───────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.settings_language),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeToggleButton(
                    label    = stringResource(R.string.settings_language_english),
                    selected = !isMacedonian,
                    modifier = Modifier.weight(1f),
                    onClick  = {
                        isMacedonian = false
                        LocaleUtils.setLocale("en")
                    }
                )
                Spacer(Modifier.width(12.dp))
                ThemeToggleButton(
                    label    = stringResource(R.string.settings_language_macedonian),
                    selected = isMacedonian,
                    modifier = Modifier.weight(1f),
                    onClick  = {
                        isMacedonian = true
                        LocaleUtils.setLocale("mk")
                    }
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text  = stringResource(R.string.settings_language_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(28.dp))

            // ── Theme ──────────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.settings_theme),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ThemeToggleButton(
                    label    = stringResource(R.string.settings_theme_light),
                    selected = themeMode == ThemeMode.LIGHT,
                    modifier = Modifier.weight(1f),
                    onClick  = { themeViewModel.setTheme(ThemeMode.LIGHT) }
                )
                Spacer(Modifier.width(8.dp))
                ThemeToggleButton(
                    label    = stringResource(R.string.settings_theme_system),
                    selected = themeMode == ThemeMode.SYSTEM,
                    modifier = Modifier.weight(1f),
                    onClick  = { themeViewModel.setTheme(ThemeMode.SYSTEM) }
                )
                Spacer(Modifier.width(8.dp))
                ThemeToggleButton(
                    label    = stringResource(R.string.settings_theme_dark),
                    selected = themeMode == ThemeMode.DARK,
                    modifier = Modifier.weight(1f),
                    onClick  = { themeViewModel.setTheme(ThemeMode.DARK) }
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text  = stringResource(R.string.settings_theme_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
        }
    }
}

/** Reusable toggle button — FilledTonal when active, Outlined when inactive. */
@Composable
private fun ThemeToggleButton(
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
