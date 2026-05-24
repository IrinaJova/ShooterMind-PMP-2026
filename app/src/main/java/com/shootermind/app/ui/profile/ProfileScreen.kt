package com.shootermind.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.ISSFCategory
import com.shootermind.app.domain.model.TrainingGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel    : ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onSignOut           : () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text(stringResource(R.string.screen_profile)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Purple header band ─────────────────────────────────────────
            Box(
                modifier            = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment    = Alignment.BottomCenter
            ) {
                val profile = (profileState as? ProfileState.Complete)?.profile

                Box(
                    modifier         = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile?.profilePictureUri != null) {
                        AsyncImage(
                            model              = profile.profilePictureUri,
                            contentDescription = "Profile picture",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier           = Modifier.size(52.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(52.dp))

            when (val state = profileState) {
                is ProfileState.Complete -> {
                    val p = state.profile

                    // ── Name + ISSF badge ──────────────────────────────────
                    Text(
                        text       = "${p.firstName} ${p.lastName}",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))

                    val issfText = when (p.issfCategory) {
                        ISSFCategory.YOUTH  -> stringResource(R.string.issf_youth)
                        ISSFCategory.JUNIOR -> stringResource(R.string.issf_junior)
                        ISSFCategory.SENIOR -> stringResource(R.string.issf_senior)
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text  = "ISSF $issfText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(20.dp))

                    // ── Info cards ─────────────────────────────────────────
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
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
                            label = stringResource(R.string.label_personal_best),
                            value = "%.1f pts".format(p.personalBest)
                        )

                        // Goal card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors   = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    stringResource(R.string.label_goal),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    goalLabel(p.goal),
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        stringResource(R.string.profile_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Sign out ───────────────────────────────────────────────────
            Button(
                onClick  = {
                    profileViewModel.signOut()
                    onSignOut()
                },
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor   = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(stringResource(R.string.settings_sign_out), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon : androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment   = Alignment.CenterVertically,
        modifier            = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun goalLabel(goal: TrainingGoal): String = when (goal) {
    TrainingGoal.IMPROVE_SCORE    -> stringResource(R.string.goal_improve_score)
    TrainingGoal.CONSISTENCY      -> stringResource(R.string.goal_consistency)
    TrainingGoal.TECHNIQUE        -> stringResource(R.string.goal_technique)
    TrainingGoal.COMPETITION_PREP -> stringResource(R.string.goal_competition_prep)
    TrainingGoal.MENTAL_TRAINING  -> stringResource(R.string.goal_mental)
}
