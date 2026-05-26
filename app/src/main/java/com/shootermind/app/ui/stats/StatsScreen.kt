package com.shootermind.app.ui.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Purple700 = Color(0xFF6D28D9)
private val Purple500 = Color(0xFF8B5CF6)

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
            // Centre-constrain content on tablets (≥ 840 dp wide)
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier            = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
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

                    // ── Score progress line chart ──────────────────────────────
                    if (stats.chartPoints.isNotEmpty()) {
                        SectionTitle(stringResource(R.string.stats_score_progress))
                        ScoreProgressChart(points = stats.chartPoints)
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
                } // end Column
            } // end Box
        } // end else
    }
}

// ── Section title ─────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary
    )
}

// ── Stat card ─────────────────────────────────────────────────────────────────

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

// ── Score progress line chart ─────────────────────────────────────────────────

/**
 * Line chart showing score trend over the last up-to-20 sessions.
 * [points] is a list of (dateMs, totalScore) sorted oldest → newest.
 */
@Composable
private fun ScoreProgressChart(points: List<Pair<Long, Double>>) {
    if (points.isEmpty()) return

    val minScore   = points.minOf { it.second }
    val maxScore   = points.maxOf { it.second }
    val scoreRange = if ((maxScore - minScore) < 1.0) 1.0 else (maxScore - minScore)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Y-axis range labels
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "%.0f".format(maxScore),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "%.0f".format(minScore),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val w      = size.width
                val h      = size.height
                val padTop = 12.dp.toPx()
                val padBot = 12.dp.toPx()
                val chartH = h - padTop - padBot
                val n      = points.size

                // X positions (evenly distributed)
                val xPos: List<Float> = if (n == 1) {
                    listOf(w / 2f)
                } else {
                    points.indices.map { i -> i.toFloat() / (n - 1).toFloat() * w }
                }

                // Y positions — higher score → lower y value (higher on screen)
                val yPos: List<Float> = points.map { (_, score) ->
                    val fraction = ((score - minScore) / scoreRange).toFloat()
                    padTop + chartH * (1f - fraction)
                }

                // Filled area under the line
                val fillPath = Path()
                xPos.forEachIndexed { i, x ->
                    if (i == 0) fillPath.moveTo(x, yPos[i]) else fillPath.lineTo(x, yPos[i])
                }
                fillPath.lineTo(xPos.last(),  padTop + chartH)
                fillPath.lineTo(xPos.first(), padTop + chartH)
                fillPath.close()

                drawPath(
                    path  = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Purple500.copy(alpha = 0.35f), Purple500.copy(alpha = 0f)),
                        startY = padTop,
                        endY   = padTop + chartH
                    )
                )

                // Line connecting the dots
                if (n > 1) {
                    val linePath = Path()
                    xPos.forEachIndexed { i, x ->
                        if (i == 0) linePath.moveTo(x, yPos[i]) else linePath.lineTo(x, yPos[i])
                    }
                    drawPath(
                        path  = linePath,
                        color = Purple500,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap   = StrokeCap.Round,
                            join  = StrokeJoin.Round
                        )
                    )
                }

                // Dots (white ring + purple fill)
                xPos.forEachIndexed { i, x ->
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(x, yPos[i]))
                    drawCircle(color = Purple700,   radius = 3.dp.toPx(), center = Offset(x, yPos[i]))
                }
            }

            Spacer(Modifier.height(4.dp))

            // X-axis date labels: first · middle · last
            if (points.size >= 2) {
                val dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = dateFmt.format(Date(points.first().first)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (points.size > 2) {
                        Text(
                            text  = dateFmt.format(Date(points[points.size / 2].first)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text  = dateFmt.format(Date(points.last().first)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Discipline card ───────────────────────────────────────────────────────────

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
