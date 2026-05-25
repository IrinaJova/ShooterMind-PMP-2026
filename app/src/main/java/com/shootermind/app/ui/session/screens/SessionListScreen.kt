package com.shootermind.app.ui.session.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import com.shootermind.app.ui.home.SessionCard
import com.shootermind.app.ui.session.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SessionFilter { ALL, RIFLE, PISTOL, COMPETITION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    onStartNewSession: () -> Unit,
    onSessionClick   : (String) -> Unit = {},
    sessionViewModel : SessionViewModel = viewModel()
) {
    val allSessions by sessionViewModel.sessions.collectAsState()

    var query         by remember { mutableStateOf("") }
    var activeFilter  by remember { mutableStateOf(SessionFilter.ALL) }

    // Apply filter + search
    val filtered = remember(allSessions, query, activeFilter) {
        allSessions
            .filter { session ->
                when (activeFilter) {
                    SessionFilter.ALL         -> true
                    SessionFilter.RIFLE       -> session.discipline == Discipline.AIR_RIFLE
                    SessionFilter.PISTOL      -> session.discipline == Discipline.AIR_PISTOL
                    SessionFilter.COMPETITION -> session.isCompetition
                }
            }
            .filter { session ->
                if (query.isBlank()) true
                else {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(session.dateMs))
                    val disciplineStr = session.discipline.name.lowercase()
                    val q = query.lowercase()
                    dateStr.lowercase().contains(q) ||
                    disciplineStr.contains(q) ||
                    session.notes.lowercase().contains(q)
                }
            }
            .sortedByDescending { it.dateMs }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Search bar ────────────────────────────────────────────────
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text(stringResource(R.string.sessions_search_hint)) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Filter chips ──────────────────────────────────────────────
            // stringResource must be called in @Composable scope, before the LazyListScope DSL
            val filters = listOf(
                SessionFilter.ALL         to stringResource(R.string.sessions_filter_all),
                SessionFilter.RIFLE       to stringResource(R.string.sessions_filter_rifle),
                SessionFilter.PISTOL      to stringResource(R.string.sessions_filter_pistol),
                SessionFilter.COMPETITION to stringResource(R.string.sessions_filter_competition)
            )
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(filters) { (filter, label) ->
                    FilterChip(
                        selected    = activeFilter == filter,
                        onClick     = { activeFilter = filter },
                        label       = { Text(label) },
                        leadingIcon = if (activeFilter == filter) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Session list ──────────────────────────────────────────────
            if (filtered.isEmpty()) {
                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text      = stringResource(R.string.sessions_empty),
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            onClick = { onSessionClick(session.id) }
                        )
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }
}
