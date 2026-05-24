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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shootermind.app.R
import com.shootermind.app.core.util.LocaleUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    // Track current language so the UI reacts immediately before restart
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                // English button
                if (!isMacedonian) {
                    FilledTonalButton(
                        onClick  = {},
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_language_english)) }
                } else {
                    OutlinedButton(
                        onClick  = {
                            isMacedonian = false
                            LocaleUtils.setLocale("en")
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_language_english)) }
                }

                Spacer(Modifier.width(12.dp))

                // Macedonian button
                if (isMacedonian) {
                    FilledTonalButton(
                        onClick  = {},
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_language_macedonian)) }
                } else {
                    OutlinedButton(
                        onClick  = {
                            isMacedonian = true
                            LocaleUtils.setLocale("mk")
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.settings_language_macedonian)) }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text  = stringResource(R.string.settings_language_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
        }
    }
}
