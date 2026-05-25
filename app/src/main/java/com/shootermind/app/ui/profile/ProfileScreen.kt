package com.shootermind.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.ISSFCategory
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.ui.stats.StatsData
import com.shootermind.app.ui.stats.StatsViewModel

private val Purple700 = Color(0xFF6D28D9)
private val Purple500 = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel    : ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onSignOut           : () -> Unit,
    statsViewModel      : StatsViewModel = viewModel()
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val stats        by statsViewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text(stringResource(R.string.screen_profile)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Purple700,
                    titleContentColor = Color.White
                ),
                actions = {
                    TextButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint               = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // ── Hero section: gradient header + overlapping avatar ────────
            // Outer Box = 140dp gradient + 48dp avatar extension below = 188dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
            ) {
                // Gradient fills only the top 140dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Brush.linearGradient(listOf(Purple700, Purple500)))
                ) {
                    // Athlete name overlay (bottom-start, inset to leave room for avatar)
                    val profile = (profileState as? ProfileState.Complete)?.profile
                    if (profile != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 128.dp, bottom = 12.dp)
                        ) {
                            Text(
                                text       = "${profile.firstName} ${profile.lastName}",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                            val issfText = when (profile.issfCategory) {
                                ISSFCategory.YOUTH  -> stringResource(R.string.issf_youth)
                                ISSFCategory.JUNIOR -> stringResource(R.string.issf_junior)
                                ISSFCategory.SENIOR -> stringResource(R.string.issf_senior)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text       = "ISSF $issfText",
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Avatar: bottom of outer Box = bottom of avatar (extends 48dp below gradient)
                Box(
                    modifier         = Modifier
                        .padding(start = 20.dp)
                        .align(Alignment.BottomStart)
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(3.dp, Purple500, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUri =
                        (profileState as? ProfileState.Complete)?.profile?.profilePictureUri
                    if (photoUri != null) {
                        AsyncImage(
                            model              = photoUri,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier           = Modifier.size(52.dp),
                            tint               = Purple500
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Body content ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                when (val state = profileState) {
                    is ProfileState.Complete -> {
                        val p = state.profile

                        // ── Stats grid ─────────────────────────────────────
                        Text(
                            text       = stringResource(R.string.profile_stats_header),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileStatTile(
                                value    = stats.totalSessions.toString(),
                                label    = stringResource(R.string.stats_total_sessions),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatTile(
                                value    = "%.1f".format(stats.bestScore),
                                label    = stringResource(R.string.stats_best_score),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val hours = stats.totalTrainingMinutes / 60
                            ProfileStatTile(
                                value    = "${hours}h",
                                label    = stringResource(R.string.profile_total_hours),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStatTile(
                                value    = if (stats.weeklyAverageScore > 0)
                                               "%.1f".format(stats.weeklyAverageScore)
                                           else "–",
                                label    = stringResource(R.string.profile_sessions_week),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ── Goal progress bar ──────────────────────────────
                        GoalProgressCard(
                            personalBest = p.personalBest,
                            stats        = stats
                        )

                        // ── Achievements ───────────────────────────────────
                        val earned = computeAchievements(stats)
                        if (earned.isNotEmpty()) {
                            Text(
                                text       = stringResource(R.string.profile_achievements),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            // Two badges per row — no FlowRow needed
                            earned.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { badgeLabel ->
                                        AchievementBadge(
                                            label    = badgeLabel,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Pad with empty space if odd number in last row
                                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        // ── Info rows ──────────────────────────────────────
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors   = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier            = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProfileInfoRow(
                                    icon  = Icons.Default.SportsScore,
                                    label = stringResource(R.string.new_session_discipline),
                                    value = when (p.discipline) {
                                        Discipline.AIR_RIFLE  -> stringResource(R.string.discipline_air_rifle)
                                        Discipline.AIR_PISTOL -> stringResource(R.string.discipline_air_pistol)
                                    }
                                )
                                ProfileInfoRow(
                                    icon  = Icons.Default.EmojiEvents,
                                    label = stringResource(R.string.label_goal),
                                    value = goalLabel(p)
                                )
                            }
                        }
                    }

                    else -> {
                        Box(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment    = Alignment.Center
                        ) {
                            Text(
                                text  = stringResource(R.string.profile_no_data),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ── Sign out ───────────────────────────────────────────────
                Button(
                    onClick  = {
                        profileViewModel.signOut()
                        onSignOut()
                    },
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor   = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(R.string.settings_sign_out),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileStatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GoalProgressCard(personalBest: Double, stats: StatsData) {
    val currentBest  = maxOf(personalBest, stats.bestScore)
    val milestone    = nextMilestone(currentBest)
    val progress     = if (milestone > 0) (currentBest / milestone).toFloat().coerceIn(0f, 1f) else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(R.string.profile_goal_progress),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = stringResource(R.string.profile_next_milestone, milestone),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            // Custom progress bar — avoids LinearProgressIndicator API version differences
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFDDD6FE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Brush.horizontalGradient(listOf(Purple700, Purple500)))
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "%.1f / %.1f".format(currentBest, milestone),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun nextMilestone(current: Double): Double {
    val milestones = listOf(400.0, 500.0, 550.0, 575.0, 590.0, 600.0, 620.0, 630.0)
    return milestones.firstOrNull { it > current } ?: (current + 10.0)
}

@Composable
private fun AchievementBadge(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier
            .background(
                Brush.linearGradient(listOf(Purple700, Purple500)),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelMedium,
            color     = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon : androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint               = Purple500,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun goalLabel(profile: com.shootermind.app.domain.model.UserProfile): String =
    when (profile.goal) {
        TrainingGoal.IMPROVE_SCORE    -> stringResource(R.string.goal_improve_score)
        TrainingGoal.CONSISTENCY      -> stringResource(R.string.goal_consistency)
        TrainingGoal.TECHNIQUE        -> stringResource(R.string.goal_technique)
        TrainingGoal.COMPETITION_PREP -> stringResource(R.string.goal_competition_prep)
        TrainingGoal.MENTAL_TRAINING  -> stringResource(R.string.goal_mental)
    }

private fun computeAchievements(stats: StatsData): List<String> {
    val badges = mutableListOf<String>()
    if (stats.totalSessions >= 1)               badges.add("🎯 First Session")
    if (stats.streak >= 3)                       badges.add("📈 Consistent Athlete")
    if (stats.totalTrainingMinutes >= 300)        badges.add("🔥 Dedicated")
    if (stats.bestScore >= 580.0)                badges.add("🏆 Sharpshooter")
    if (stats.totalSessions >= 20)               badges.add("⭐ Season Veteran")
    if (stats.totalSessions >= 50)               badges.add("🌟 Elite Shooter")
    return badges
}
