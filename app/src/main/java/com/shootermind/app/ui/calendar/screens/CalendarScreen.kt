package com.shootermind.app.ui.calendar.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.CalendarEvent
import com.shootermind.app.domain.model.EventType
import com.shootermind.app.ui.calendar.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Light-mode type colours (used for dots and badges)
private val TrainingLight    = Color(0xFF6D28D9)
private val CompetitionLight = Color(0xFFDC2626)
private val RecoveryLight    = Color(0xFF16A34A)
private val OtherLight       = Color(0xFF6B7280)

// Dark-mode type colours — lighter so they're readable on dark surfaces
private val TrainingDark    = Color(0xFFCFBCFF)
private val CompetitionDark = Color(0xFFFCA5A5)
private val RecoveryDark    = Color(0xFF86EFAC)
private val OtherDark       = Color(0xFFD1D5DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onAddEvent    : () -> Unit,
    onEditEvent   : (String) -> Unit,
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val allEvents by calendarViewModel.allEvents.collectAsState()

    // Month navigation state
    var displayedCal by remember {
        mutableStateOf(Calendar.getInstance().also {
            it.set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // Filter events for the displayed month
    val monthStart = (displayedCal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val monthEnd = (displayedCal.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    val monthEvents = allEvents.filter { it.dateTimeMs in monthStart..monthEnd }

    // Group events by date string
    val dateFmt = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    val grouped = monthEvents.groupBy { dateFmt.format(Date(it.dateTimeMs)) }
        .toSortedMap(compareBy { it })

    var deleteTarget by remember { mutableStateOf<CalendarEvent?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.screen_calendar)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEvent) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.calendar_add_event))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Month navigation bar ──────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    displayedCal = (displayedCal.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
                Text(
                    text       = monthFmt.format(displayedCal.time).replaceFirstChar { it.uppercaseChar() },
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    displayedCal = (displayedCal.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }

            if (grouped.isEmpty()) {
                // ── Empty state ───────────────────────────────────────────
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = stringResource(R.string.calendar_no_events),
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grouped.forEach { (dateLabel, events) ->
                        // Date header
                        item(key = "header_$dateLabel") {
                            Text(
                                text       = dateLabel,
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(events, key = { it.id }) { event ->
                            EventCard(
                                event    = event,
                                onClick  = { onEditEvent(event.id) },
                                onDelete = { deleteTarget = event }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteTarget?.let { event ->
        AlertDialog(
            onDismissRequest  = { deleteTarget = null },
            title             = { Text(stringResource(R.string.calendar_delete_event)) },
            text              = { Text(stringResource(R.string.calendar_delete_confirm)) },
            confirmButton     = {
                TextButton(onClick = {
                    calendarViewModel.deleteEvent(event)
                    deleteTarget = null
                }) {
                    Text(
                        stringResource(R.string.btn_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton     = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun EventCard(
    event   : CalendarEvent,
    onClick : () -> Unit,
    onDelete: () -> Unit
) {
    val timeFmt   = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isDark    = isSystemInDarkTheme()
    val typeColor = when (event.eventType) {
        EventType.TRAINING    -> if (isDark) TrainingDark    else TrainingLight
        EventType.COMPETITION -> if (isDark) CompetitionDark else CompetitionLight
        EventType.RECOVERY    -> if (isDark) RecoveryDark    else RecoveryLight
        EventType.OTHER       -> if (isDark) OtherDark       else OtherLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type colour dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(typeColor)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val detail = buildString {
                    append(timeFmt.format(Date(event.dateTimeMs)))
                    if (event.location.isNotBlank()) append("  ·  ${event.location}")
                }
                Text(
                    text  = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text  = eventTypeLabel(event.eventType),
                    style = MaterialTheme.typography.labelSmall,
                    color = typeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.calendar_delete_event),
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun eventTypeLabel(type: EventType): String = when (type) {
    EventType.TRAINING    -> stringResource(R.string.calendar_type_training)
    EventType.COMPETITION -> stringResource(R.string.calendar_type_competition)
    EventType.RECOVERY    -> stringResource(R.string.calendar_type_recovery)
    EventType.OTHER       -> stringResource(R.string.calendar_type_other)
}

@Composable
private fun eventTypeColor(type: EventType): Color {
    val isDark = isSystemInDarkTheme()
    return when (type) {
        EventType.TRAINING    -> if (isDark) TrainingDark    else TrainingLight
        EventType.COMPETITION -> if (isDark) CompetitionDark else CompetitionLight
        EventType.RECOVERY    -> if (isDark) RecoveryDark    else RecoveryLight
        EventType.OTHER       -> if (isDark) OtherDark       else OtherLight
    }
}
