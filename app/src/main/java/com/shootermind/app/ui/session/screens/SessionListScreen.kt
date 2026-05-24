package com.shootermind.app.ui.session.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.shootermind.app.ui.session.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    onStartNewSession: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel()
) {
    val sessions by sessionViewModel.sessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.screen_sessions)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartNewSession) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.session_add_fab))
            }
        }
    ) { innerPadding ->
        if (sessions.isEmpty()) {
            Text(
                text      = stringResource(R.string.sessions_empty),
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp)
            )
        } else {
            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionListItem(
                        session  = session,
                        onDelete = { sessionViewModel.deleteSession(session) }
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
private fun SessionListItem(session: TrainingSession, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())
        .format(Date(session.dateMs))
    val disciplineLabel = when (session.discipline) {
        Discipline.AIR_RIFLE  -> "10m Air Rifle"
        Discipline.AIR_PISTOL -> "10m Air Pistol"
    }

    ListItem(
        headlineContent   = { Text(disciplineLabel, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(dateStr) },
        trailingContent   = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = "%.1f".format(session.totalScore),
                    style      = MaterialTheme.typography.titleMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint               = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
