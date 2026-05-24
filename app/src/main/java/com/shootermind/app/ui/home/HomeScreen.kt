package com.shootermind.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import com.shootermind.app.ui.profile.ProfileState
import com.shootermind.app.ui.profile.ProfileViewModel
import com.shootermind.app.ui.session.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartNewSession: () -> Unit,
    sessionViewModel : SessionViewModel  = viewModel(),
    profileViewModel : ProfileViewModel  = viewModel()
) {
    val recentSessions by sessionViewModel.recentSessions.collectAsState()
    val profileState   by profileViewModel.profileState.collectAsState()

    // Resolve first name — falls back to "Shooter"
    val firstName = when (val s = profileState) {
        is ProfileState.Complete -> s.profile.firstName.ifBlank { null }
        else                     -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text(stringResource(R.string.screen_home)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (recentSessions.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onStartNewSession,
                    icon    = { Icon(Icons.Default.Add, contentDescription = null) },
                    text    = { Text(stringResource(R.string.home_start_session)) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }

            // ── Welcome header ─────────────────────────────────────────────
            item {
                val welcomeText = if (firstName != null)
                    stringResource(R.string.home_welcome_name, firstName)
                else
                    stringResource(R.string.home_welcome)

                Text(
                    text       = welcomeText,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Empty state — first session CTA ───────────────────────────
            if (recentSessions.isEmpty()) {
                item {
                    FirstSessionCta(onStartNewSession)
                }
            } else {
                // ── Recent sessions section ────────────────────────────────
                item {
                    Text(
                        text       = stringResource(R.string.home_recent_sessions),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(recentSessions) { session ->
                    SessionCard(session)
                }
                item { Spacer(Modifier.height(88.dp)) } // FAB clearance
            }
        }
    }
}

// ── First session call-to-action ────────────────────────────────────────────

@Composable
private fun FirstSessionCta(onStartNewSession: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text       = stringResource(R.string.home_first_session_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.home_first_session_body),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick  = onStartNewSession,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(
                    text       = stringResource(R.string.home_first_session_cta),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Session card ─────────────────────────────────────────────────────────────

@Composable
fun SessionCard(session: TrainingSession) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        .format(Date(session.dateMs))

    val timeStr = if (session.startHour >= 0 && session.endHour >= 0) {
        "  ·  %02d:%02d – %02d:%02d".format(
            session.startHour, session.startMinute,
            session.endHour, session.endMinute
        )
    } else ""

    val disciplineLabel = when (session.discipline) {
        Discipline.AIR_RIFLE  -> "10m Air Rifle"
        Discipline.AIR_PISTOL -> "10m Air Pistol"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = disciplineLabel,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (session.isCompetition) {
                            Icon(
                                imageVector        = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.size(4.dp))
                        }
                        Text(
                            text  = "$dateStr$timeStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text       = "%.1f".format(session.totalScore),
                    style      = MaterialTheme.typography.titleLarge,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (session.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = session.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}
