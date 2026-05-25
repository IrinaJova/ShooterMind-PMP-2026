package com.shootermind.app.ui.stats

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R

private val Purple700 = Color(0xFF6D28D9)
private val Purple500 = Color(0xFF8B5CF6)
private val Purple200 = Color(0xFFDDD6FE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(statsViewModel: StatsViewModel = viewModel()) {
    val stats by statsViewModel.stats.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.screen_stats)) }) }
    ) { innerPadding ->
        if (stats.totalSessions == 0) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text      = stringResource(R.string.stats_no_data),
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Overview ───────────────────────────────────────────────
                SectionTitle(stringResource(R.string.stats_overview))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label    = stringResource(R.string.stats_total_sessions),
                        value    = stats.totalSessions.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = stringResource(R.string.stats_best_score),
                        value    = "%.1f".format(stats.bestScore),
                        modifier = Modifier.weight(1f)
                    )
                }
                StatCard(
                    label    = stringResource(R.string.stats_average_score),
                    value    = "%.2f".format(stats.averageScore),
                    modifier = Modifier.fillMaxWidth()
                )

                // ── This Week ──────────────────────────────────────────────
                SectionTitle(stringResource(R.string.home_quick_stats))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label    = stringResource(R.string.stats_weekly_sessions),
                        value    = stats.weeklySessionCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = stringResource(R.string.stats_streak),
                        value    = stringResource(R.string.stats_days, stats.streak),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val hours = stats.totalTrainingMinutes / 60
                    StatCard(
                        label    = stringResource(R.string.stats_total_hours),
                        value    = "${hours}h",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = stringResource(R.string.stats_competitions),
                        value    = stats.competitionCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Recent trend ───────────────────────────────────────────
                if (stats.recentScores.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.stats_recent_trend))
                    TrendBarChart(scores = stats.recentScores)
                }

                // ── Per discipline ─────────────────────────────────────────
                if (stats.rifleCount > 0 || stats.pistolCount > 0) {
                    SectionTitle(stringResource(R.string.stats_by_discipline))

                    if (stats.rifleCount > 0) {
                        DisciplineCard(
                            name    = stringResource(R.string.discipline_air_rifle),
                            count   = stats.rifleCount,
                            average = stats.rifleAvg
                        )
                    }
                    if (stats.pistolCount > 0) {
                        DisciplineCard(
                            name    = stringResource(R.string.discipline_air_pistol),
                            count   = stats.pistolCount,
                            average = stats.pistolAvg
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
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
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = label,
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TrendBarChart(scores: List<Double>) {
    if (scores.isEmpty()) return

    val barColor = Brush.verticalGradient(listOf(Purple500, Purple200))
    val minScore = scores.min()
    val maxScore = scores.max()
    val range    = (maxScore - minScore).let { if (it < 1.0) 1.0 else it }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bar chart
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.Bottom
            ) {
                scores.forEach { score ->
                    val fraction = ((score - minScore) / range).toFloat().coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((fraction * 100).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Brush.verticalGradient(listOf(Purple500, Purple200)))
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            // X-axis labels
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                scores.forEachIndexed { idx, score ->
                    Text(
                        text      = "%.0f".format(score),
                        style     = MaterialTheme.typography.labelSmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.weight(1f),
                        maxLines  = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DisciplineCard(name: String, count: Int, average: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    name,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$count sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "avg %.1f".format(average),
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
