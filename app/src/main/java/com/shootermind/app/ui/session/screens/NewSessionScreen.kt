package com.shootermind.app.ui.session.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.ui.session.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionScreen(
    onNavigateBack: () -> Unit,
    sessionViewModel: SessionViewModel = viewModel()
) {
    var selectedDiscipline by remember { mutableStateOf(Discipline.AIR_RIFLE) }
    var totalScoreText     by remember { mutableStateOf("") }
    var notes              by remember { mutableStateOf("") }
    var scoreError         by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_new_session)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back)
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            // ── Discipline ────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.new_session_discipline),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            Discipline.entries.forEach { discipline ->
                val label = when (discipline) {
                    Discipline.AIR_RIFLE  -> stringResource(R.string.discipline_air_rifle)
                    Discipline.AIR_PISTOL -> stringResource(R.string.discipline_air_pistol)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedDiscipline == discipline,
                        onClick  = { selectedDiscipline = discipline }
                    )
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Total score ───────────────────────────────────────────────
            OutlinedTextField(
                value           = totalScoreText,
                onValueChange   = { totalScoreText = it; scoreError = false },
                label           = { Text(stringResource(R.string.stats_average_score)) },
                placeholder     = { Text("0.0") },
                singleLine      = true,
                isError         = scoreError,
                supportingText  = if (scoreError) {
                    { Text(stringResource(R.string.error_field_required), color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier        = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Notes ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text("Notes (optional)") },
                minLines      = 3,
                maxLines      = 5,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            // ── Save ──────────────────────────────────────────────────────
            Button(
                onClick = {
                    val score = totalScoreText.toDoubleOrNull()
                    if (score == null) {
                        scoreError = true
                    } else {
                        sessionViewModel.addSession(
                            discipline = selectedDiscipline,
                            totalScore = score,
                            notes      = notes.trim()
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_save))
            }
        }
    }
}
