package com.shootermind.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.domain.model.TrainingSession
import com.shootermind.app.domain.model.CalendarEvent
import com.shootermind.app.domain.model.EventType
import com.shootermind.app.ui.calendar.CalendarViewModel
import com.shootermind.app.ui.profile.ProfileState
import com.shootermind.app.ui.profile.ProfileViewModel
import com.shootermind.app.ui.session.SessionViewModel
import com.shootermind.app.ui.stats.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val Purple700  = Color(0xFF6D28D9)
private val Purple500  = Color(0xFF8B5CF6)
private val Amber400   = Color(0xFFFBBF24)
private val White80    = Color(0xCCFFFFFF)
private val White60    = Color(0x99FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartNewSession: () -> Unit,
    onSessionClick   : (String) -> Unit  = {},
    onViewStats      : () -> Unit         = {},
    onOpenCalendar   : () -> Unit         = {},
    sessionViewModel : SessionViewModel   = viewModel(),
    profileViewModel : ProfileViewModel   = viewModel(),
    statsViewModel   : StatsViewModel     = viewModel(),
    calendarViewModel: CalendarViewModel  = viewModel()
) {
    val recentSessions  by sessionViewModel.recentSessions.collectAsState()
    val profileState    by profileViewModel.profileState.collectAsState()
    val stats           by statsViewModel.stats.collectAsState()
    val upcomingEvents  by calendarViewModel.upcomingEvents.collectAsState()

    val firstName = when (val s = profileState) {
        is ProfileState.Complete -> s.profile.firstName.ifBlank { null }
        else                     -> null
    }
    val goal = when (val s = profileState) {
        is ProfileState.Complete -> s.profile.goal
        else                     -> null
    }

    // Daily quote index (rotates each day)
    val quoteIndex = remember { (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 5) }

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
                    icon    = { Icon(Icons.Default.Add, null) },
                    text    = { Text(stringResource(R.string.home_start_session)) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Hero card ─────────────────────────────────────────────────
            item {
                HeroCard(
                    firstName  = firstName,
                    goal       = goal,
                    streak     = stats.streak,
                    quoteIndex = quoteIndex
                )
            }

            // ── Quick stats row ───────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text       = "📊",
                    fontSize   = 14.sp,
                    modifier   = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val lastScore = recentSessions.firstOrNull()?.totalScore
                    item {
                        QuickStatChip(
                            label = stringResource(R.string.home_last_score),
                            value = if (lastScore != null) "%.1f".format(lastScore)
                                    else stringResource(R.string.home_no_score)
                        )
                    }
                    item {
                        QuickStatChip(
                            label = stringResource(R.string.stats_best_score),
                            value = if (stats.bestScore > 0) "%.1f".format(stats.bestScore)
                                    else stringResource(R.string.home_no_score)
                        )
                    }
                    item {
                        QuickStatChip(
                            label = stringResource(R.string.home_weekly_avg),
                            value = if (stats.weeklyAverageScore > 0) "%.1f".format(stats.weeklyAverageScore)
                                    else stringResource(R.string.home_no_score)
                        )
                    }
                    item {
                        QuickStatChip(
                            label = stringResource(R.string.home_this_week),
                            value = stats.weeklySessionCount.toString()
                        )
                    }
                }
            }

            // ── Quick actions ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon     = Icons.Default.Add,
                        label    = stringResource(R.string.home_start_session),
                        onClick  = onStartNewSession,
                        modifier = Modifier.weight(1f),
                        primary  = true
                    )
                    QuickActionCard(
                        icon     = Icons.Default.BarChart,
                        label    = stringResource(R.string.home_view_stats),
                        onClick  = onViewStats,
                        modifier = Modifier.weight(1f),
                        primary  = false
                    )
                    QuickActionCard(
                        icon     = Icons.Default.CalendarMonth,
                        label    = stringResource(R.string.nav_calendar),
                        onClick  = onOpenCalendar,
                        modifier = Modifier.weight(1f),
                        primary  = false
                    )
                }
            }

            // ── Upcoming Events ───────────────────────────────────────────
            if (upcomingEvents.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text       = stringResource(R.string.home_upcoming_events),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text     = stringResource(R.string.home_see_all),
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(onClick = onOpenCalendar)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    upcomingEvents.take(3).forEach { event ->
                        UpcomingEventRow(
                            event    = event,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // ── Empty first-session CTA ───────────────────────────────────
            if (recentSessions.isEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    FirstSessionCta(
                        onStart  = onStartNewSession,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // ── Recent sessions ────────────────────────────────────────
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text       = stringResource(R.string.home_recent_sessions),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(recentSessions) { session ->
                    SessionCard(
                        session  = session,
                        onClick  = { onSessionClick(session.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

// ── Hero card ───────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(
    firstName  : String?,
    goal       : TrainingGoal?,
    streak     : Int,
    quoteIndex : Int
) {
    val gradient = Brush.linearGradient(listOf(Purple500, Purple700))

    val welcomeText = if (firstName != null)
        stringResource(R.string.home_welcome_name, firstName)
    else
        stringResource(R.string.home_welcome)

    val focusText = when (goal) {
        TrainingGoal.IMPROVE_SCORE    -> stringResource(R.string.focus_improve_score)
        TrainingGoal.CONSISTENCY      -> stringResource(R.string.focus_consistency)
        TrainingGoal.TECHNIQUE        -> stringResource(R.string.focus_technique)
        TrainingGoal.COMPETITION_PREP -> stringResource(R.string.focus_competition_prep)
        TrainingGoal.MENTAL_TRAINING  -> stringResource(R.string.focus_mental)
        null                          -> stringResource(R.string.focus_default)
    }

    val quote = stringResource(
        when (quoteIndex) {
            0    -> R.string.quote_0
            1    -> R.string.quote_1
            2    -> R.string.quote_2
            3    -> R.string.quote_3
            else -> R.string.quote_4
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            // Streak badge
            if (streak > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint     = Amber400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text  = "  " + stringResource(R.string.home_streak_days, streak),
                        style = MaterialTheme.typography.labelMedium,
                        color = Amber400,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // Welcome
            Text(
                text       = welcomeText,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
            Spacer(Modifier.height(8.dp))

            // Today's focus label
            Text(
                text  = stringResource(R.string.home_today_focus).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = White60,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = focusText,
                style      = MaterialTheme.typography.bodyLarge,
                color      = White80,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // Quote
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(12.dp)
            ) {
                Text(
                    text      = "\"$quote\"",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = White80,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// ── Quick stat chip ─────────────────────────────────────────────────────────

@Composable
private fun QuickStatChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Quick action card ───────────────────────────────────────────────────────

@Composable
private fun QuickActionCard(
    icon    : ImageVector,
    label   : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    primary : Boolean  = true
) {
    val bgColor = if (primary) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.secondaryContainer
    val fgColor = if (primary) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = fgColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                color      = fgColor,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ── First session CTA ───────────────────────────────────────────────────────

@Composable
private fun FirstSessionCta(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier            = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter, null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text       = stringResource(R.string.home_first_session_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.home_first_session_body),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(20.dp))
            androidx.compose.material3.Button(
                onClick  = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.home_first_session_cta), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Upcoming event row ──────────────────────────────────────────────────────

@Composable
private fun UpcomingEventRow(
    event   : CalendarEvent,
    modifier: Modifier = Modifier
) {
    val isDark   = isSystemInDarkTheme()
    val dotColor = when (event.eventType) {
        EventType.TRAINING    -> if (isDark) Color(0xFFCFBCFF) else Purple700
        EventType.COMPETITION -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)
        EventType.RECOVERY    -> if (isDark) Color(0xFF86EFAC) else Color(0xFF16A34A)
        EventType.OTHER       -> if (isDark) Color(0xFFD1D5DB) else Color(0xFF9CA3AF)
    }
    val dateStr = remember(event.dateTimeMs) {
        SimpleDateFormat("EEE, d MMM  •  HH:mm", Locale.getDefault())
            .format(Date(event.dateTimeMs))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Session card ─────────────────────────────────────────────────────────────

@Composable
fun SessionCard(
    session : TrainingSession,
    onClick : () -> Unit      = {},
    modifier: Modifier        = Modifier
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(session.dateMs))
    val disciplineLabel = when (session.discipline) {
        Discipline.AIR_RIFLE  -> stringResource(R.string.discipline_air_rifle)
        Discipline.AIR_PISTOL -> stringResource(R.string.discipline_air_pistol)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = disciplineLabel,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (session.isCompetition) {
                        Spacer(Modifier.size(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🏆", fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = buildString {
                        append(dateStr)
                        if (session.startHour >= 0)
                            append("  ·  %02d:%02d".format(session.startHour, session.startMinute))
                        if (session.durationMinutes > 0)
                            append("  ·  ${session.durationMinutes} min")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text       = "%.1f".format(session.totalScore),
                style      = MaterialTheme.typography.headlineSmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
