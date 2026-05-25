package com.shootermind.app.ui.session.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import com.shootermind.app.ui.session.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Purple700 = Color(0xFF6D28D9)
private val Purple500 = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(
    sessionId       : String,
    onNavigateBack  : () -> Unit,
    sessionViewModel: SessionViewModel = viewModel()
) {
    val sessions by sessionViewModel.sessions.collectAsState()
    val session  = sessions.find { it.id == sessionId }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && session != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title            = { Text(stringResource(R.string.session_details_delete)) },
            text             = { Text(stringResource(R.string.session_details_delete_confirm)) },
            confirmButton    = {
                Button(
                    onClick = {
                        sessionViewModel.deleteSession(session)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.btn_delete)) }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_session_details)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    if (session != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.session_details_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (session == null) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = stringResource(R.string.error_generic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Hero header ────────────────────────────────────────────
                SessionHeroCard(session)

                // ── Series breakdown ───────────────────────────────────────
                val series = parseSeriesData(session.seriesData)
                if (series.isNotEmpty()) {
                    SectionCard(title = stringResource(R.string.session_details_series_breakdown)) {
                        series.forEachIndexed { idx, (seriesTotal, shots) ->
                            SeriesRow(
                                index      = idx + 1,
                                total      = seriesTotal,
                                shots      = shots,
                                isDecimal  = session.useDecimalScore
                            )
                            if (idx < series.lastIndex) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        }
                    }
                } else if (session.seriesData.isNotEmpty()) {
                    // Legacy: might have data in different format
                } else {
                    SectionCard(title = stringResource(R.string.session_details_series_breakdown)) {
                        Text(
                            text  = stringResource(R.string.session_details_no_series),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Journal ────────────────────────────────────────────────
                val hasJournal = session.notes.isNotBlank() || session.batch.isNotBlank() ||
                    session.airPressure.isNotBlank() || session.muscleRecovery > 0 ||
                    session.fatigue > 0 || session.concentration > 0 ||
                    session.endurance > 0 || session.heartRate > 0

                if (hasJournal) {
                    SectionCard(title = stringResource(R.string.session_details_journal)) {
                        if (session.notes.isNotBlank()) {
                            JournalRow(label = stringResource(R.string.journal_notes),
                                value = session.notes)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (session.batch.isNotBlank()) {
                            JournalRow(label = stringResource(R.string.journal_batch),
                                value = session.batch)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (session.airPressure.isNotBlank()) {
                            JournalRow(label = stringResource(R.string.journal_air_pressure),
                                value = "${session.airPressure} bar")
                            Spacer(Modifier.height(8.dp))
                        }

                        val hasRatings = session.muscleRecovery > 0 || session.fatigue > 0 ||
                            session.concentration > 0 || session.endurance > 0 || session.heartRate > 0
                        if (hasRatings) {
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text       = stringResource(R.string.session_details_mood_section),
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            RatingDisplayRow(stringResource(R.string.journal_muscle_recovery), session.muscleRecovery)
                            RatingDisplayRow(stringResource(R.string.journal_fatigue), session.fatigue)
                            RatingDisplayRow(stringResource(R.string.journal_concentration), session.concentration)
                            RatingDisplayRow(stringResource(R.string.journal_endurance), session.endurance)
                            RatingDisplayRow(stringResource(R.string.journal_heart_rate), session.heartRate)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Hero header card ─────────────────────────────────────────────────────────

@Composable
private fun SessionHeroCard(session: TrainingSession) {
    val gradient = Brush.linearGradient(listOf(Purple500, Purple700))
    val dateStr  = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date(session.dateMs))
    val disciplineLabel = when (session.discipline) {
        Discipline.AIR_RIFLE  -> "10m Air Rifle"
        Discipline.AIR_PISTOL -> "10m Air Pistol"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(24.dp)
    ) {
        Column {
            // Discipline + badges row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = disciplineLabel,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                if (session.isCompetition) {
                    BadgeChip(stringResource(R.string.competition_badge))
                }
                if (session.isControlSession) {
                    BadgeChip(stringResource(R.string.control_badge))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Score
            Text(
                text       = "%.2f".format(session.totalScore),
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White
            )
            Text(
                text  = "${session.shotCount} shots",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(12.dp))

            // Date / time / duration
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(0.8f),
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(dateStr, style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.8f))
                }
                if (session.startHour >= 0 && session.endHour >= 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = Color.White.copy(0.8f),
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "%02d:%02d – %02d:%02d".format(
                                session.startHour, session.startMinute,
                                session.endHour, session.endMinute
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.8f)
                        )
                    }
                }
            }
            if (session.durationMinutes > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.sessions_duration_min, session.durationMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f)
                )
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x33FFFFFF))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

// ── Section card ─────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title  : String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── Series row ────────────────────────────────────────────────────────────────

@Composable
private fun SeriesRow(
    index    : Int,
    total    : Double,
    shots    : List<Double>,
    isDecimal: Boolean
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text       = stringResource(R.string.session_details_series_n, index),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (shots.isNotEmpty()) {
                val shotsText = shots.joinToString(" · ") {
                    if (isDecimal) "%.1f".format(it) else it.toInt().toString()
                }
                Text(
                    text  = shotsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text       = "%.2f".format(total),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Journal row ───────────────────────────────────────────────────────────────

@Composable
private fun JournalRow(label: String, value: String) {
    Column {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Rating display row ────────────────────────────────────────────────────────

@Composable
private fun RatingDisplayRow(label: String, rating: Int) {
    val emojis = listOf("😫", "😔", "😐", "🙂", "😄")
    if (rating == 0) return

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            emojis.forEachIndexed { idx, emoji ->
                val filled = idx < rating
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = emoji,
                        fontSize = 14.sp,
                        color    = if (filled) Color.Unspecified
                                   else Color.Unspecified.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ── Parse series data ─────────────────────────────────────────────────────────

/**
 * Returns a list of (seriesTotal, listOfIndividualShots).
 * Supports both new format "98.7|97.3" and legacy "9.8,10.2|10.1,9.9".
 */
private fun parseSeriesData(seriesData: String): List<Pair<Double, List<Double>>> {
    if (seriesData.isBlank()) return emptyList()
    return seriesData.split("|").mapNotNull { seriesStr ->
        val parts = seriesStr.split(",").mapNotNull { it.trim().toDoubleOrNull() }
        when {
            parts.isEmpty() -> null
            parts.size == 1 -> parts[0] to emptyList()  // Series total only
            else            -> parts.sum() to parts      // Legacy: individual shots
        }
    }
}
