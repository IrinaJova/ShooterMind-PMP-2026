@file:OptIn(ExperimentalMaterial3Api::class)

package com.shootermind.app.ui.session.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.ui.session.NewSessionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionScreen(onNavigateBack: () -> Unit) {

    val vm           = viewModel<NewSessionViewModel>()
    val scope        = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_session_info),
        stringResource(R.string.tab_journal)
    )

    // ── Date picker ────────────────────────────────────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    val dpState = rememberDatePickerState(initialSelectedDateMillis = vm.dateMs)
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { vm.dateMs = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.btn_save)) }
            },
            dismissButton    = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) { DatePicker(state = dpState) }
    }

    // ── Start time picker ──────────────────────────────────────────────────
    var showStartTimePicker by remember { mutableStateOf(false) }
    val startTimeState = rememberTimePickerState(
        initialHour   = if (vm.startHour >= 0) vm.startHour else 9,
        initialMinute = vm.startMinute,
        is24Hour      = true
    )
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss  = { showStartTimePicker = false },
            onConfirm  = {
                vm.startHour   = startTimeState.hour
                vm.startMinute = startTimeState.minute
                showStartTimePicker = false
            }
        ) { TimePicker(state = startTimeState) }
    }

    // ── End time picker ────────────────────────────────────────────────────
    var showEndTimePicker by remember { mutableStateOf(false) }
    val endTimeState = rememberTimePickerState(
        initialHour   = if (vm.endHour >= 0) vm.endHour else 11,
        initialMinute = vm.endMinute,
        is24Hour      = true
    )
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss  = { showEndTimePicker = false },
            onConfirm  = {
                vm.endHour   = endTimeState.hour
                vm.endMinute = endTimeState.minute
                showEndTimePicker = false
            }
        ) { TimePicker(state = endTimeState) }
    }

    // ── Error string ───────────────────────────────────────────────────────
    val errorNotEnough = stringResource(R.string.error_not_enough_data)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar       = {
            TopAppBar(
                title  = { Text(stringResource(R.string.screen_new_session)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            vm.saveSession(
                                onDone  = onNavigateBack,
                                onError = { scope.launch {
                                    snackbarHost.showSnackbar(
                                        message  = errorNotEnough,
                                        duration = SnackbarDuration.Long
                                    )
                                }}
                            )
                        }
                    ) {
                        Text(
                            text       = stringResource(R.string.btn_save_session),
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Discipline selector ────────────────────────────────────────
            DisciplineSelector(vm = vm)

            HorizontalDivider()

            // ── Tab row ────────────────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick  = { selectedTab = idx },
                        text     = { Text(title) }
                    )
                }
            }

            // ── Tab content ────────────────────────────────────────────────
            when (selectedTab) {
                0 -> SessionInfoTab(
                    vm              = vm,
                    onPickDate      = { showDatePicker = true },
                    onPickStartTime = { showStartTimePicker = true },
                    onPickEndTime   = { showEndTimePicker = true }
                )
                1 -> JournalTab(vm = vm)
            }
        }
    }
}

// ── Discipline selector ─────────────────────────────────────────────────────

@Composable
private fun DisciplineSelector(vm: NewSessionViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Discipline.entries.forEach { d ->
            val label = when (d) {
                Discipline.AIR_RIFLE  -> stringResource(R.string.discipline_air_rifle)
                Discipline.AIR_PISTOL -> stringResource(R.string.discipline_air_pistol)
            }
            FilterChip(
                selected = vm.discipline == d,
                onClick  = { vm.discipline = d },
                label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
                modifier = Modifier.weight(1f),
                leadingIcon = if (vm.discipline == d) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

// ── Session Info tab ────────────────────────────────────────────────────────

@Composable
private fun SessionInfoTab(
    vm             : NewSessionViewModel,
    onPickDate     : () -> Unit,
    onPickStartTime: () -> Unit,
    onPickEndTime  : () -> Unit
) {
    val dateLabel = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(vm.dateMs))
    val startLabel = if (vm.startHour >= 0)
        "%02d:%02d".format(vm.startHour, vm.startMinute)
    else
        stringResource(R.string.session_time_not_set)
    val endLabel = if (vm.endHour >= 0)
        "%02d:%02d".format(vm.endHour, vm.endMinute)
    else
        stringResource(R.string.session_time_not_set)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Date ───────────────────────────────────────────────────────────
        SectionLabel(stringResource(R.string.new_session_date))
        ClickableRow(
            icon    = Icons.Default.CalendarToday,
            text    = dateLabel,
            onClick = onPickDate
        )

        // ── Time ───────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel(stringResource(R.string.session_start_time))
                ClickableRow(
                    icon    = Icons.Default.AccessTime,
                    text    = startLabel,
                    onClick = onPickStartTime
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel(stringResource(R.string.session_end_time))
                ClickableRow(
                    icon    = Icons.Default.AccessTime,
                    text    = endLabel,
                    onClick = onPickEndTime
                )
            }
        }

        HorizontalDivider()

        // ── Session type toggles ───────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = vm.isCompetition,
                onClick  = { vm.isCompetition = !vm.isCompetition },
                label    = { Text(stringResource(R.string.session_competition)) },
                modifier = Modifier.weight(1f),
                leadingIcon = if (vm.isCompetition) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
            FilterChip(
                selected = vm.isControlSession,
                onClick  = { vm.isControlSession = !vm.isControlSession },
                label    = { Text(stringResource(R.string.session_control_session)) },
                modifier = Modifier.weight(1f),
                leadingIcon = if (vm.isControlSession) {
                    { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                } else null
            )
        }

        HorizontalDivider()

        // ── Result section ─────────────────────────────────────────────────
        SectionLabel(stringResource(R.string.session_result_section))

        // Decimal score toggle
        FilterChip(
            selected    = vm.useDecimalScore,
            onClick     = { vm.useDecimalScore = !vm.useDecimalScore },
            label       = { Text(stringResource(R.string.session_decimal_score)) },
            leadingIcon = if (vm.useDecimalScore) {
                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
            } else null
        )

        // Split into series toggle
        FilterChip(
            selected    = vm.splitIntoSeries,
            onClick     = { vm.splitIntoSeries = !vm.splitIntoSeries },
            label       = { Text(stringResource(R.string.session_split_series)) },
            leadingIcon = if (vm.splitIntoSeries) {
                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
            } else null
        )

        if (vm.splitIntoSeries) {
            // Series count stepper
            StepperRow(
                label       = stringResource(R.string.session_series_count),
                value       = vm.seriesCount,
                onDecrement = { vm.updateSeriesCount(vm.seriesCount - 1) },
                onIncrement = { vm.updateSeriesCount(vm.seriesCount + 1) },
                minValue    = 1
            )

            // Shots per series stepper
            StepperRow(
                label       = stringResource(R.string.session_shots_per_series),
                value       = vm.shotsPerSeries,
                onDecrement = { vm.updateShotsPerSeries(vm.shotsPerSeries - 1) },
                onIncrement = { vm.updateShotsPerSeries(vm.shotsPerSeries + 1) },
                minValue    = 1
            )

            // Series score fields
            val keyboard = if (vm.useDecimalScore)
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            else
                KeyboardOptions(keyboardType = KeyboardType.Number)

            repeat(vm.seriesCount) { idx ->
                OutlinedTextField(
                    value         = vm.seriesScores.getOrElse(idx) { "" },
                    onValueChange = { vm.updateSeriesScore(idx, it) },
                    label         = {
                        Text(stringResource(R.string.session_series_score_hint, idx + 1))
                    },
                    singleLine      = true,
                    keyboardOptions = keyboard,
                    modifier        = Modifier.fillMaxWidth()
                )
            }

            // Calculated total
            val total = vm.computedTotal
            if (total > 0.0) {
                Card(
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = stringResource(R.string.session_calculated_total),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text       = "%.2f".format(total),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            // Direct input
            val keyboard = if (vm.useDecimalScore)
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            else
                KeyboardOptions(keyboardType = KeyboardType.Number)

            OutlinedTextField(
                value           = vm.totalResultText,
                onValueChange   = { vm.totalResultText = it },
                label           = { Text(stringResource(R.string.session_total_score_label)) },
                singleLine      = true,
                keyboardOptions = keyboard,
                modifier        = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value           = vm.shotCountText,
                onValueChange   = { vm.shotCountText = it },
                label           = { Text(stringResource(R.string.session_shot_count_label)) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Journal tab ─────────────────────────────────────────────────────────────

@Composable
private fun JournalTab(vm: NewSessionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Notes ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value         = vm.notes,
            onValueChange = { vm.notes = it },
            label         = { Text(stringResource(R.string.journal_notes)) },
            placeholder   = { Text(stringResource(R.string.journal_notes_hint)) },
            minLines      = 4,
            maxLines      = 8,
            modifier      = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // ── Equipment ──────────────────────────────────────────────────────
        OutlinedTextField(
            value           = vm.batch,
            onValueChange   = { vm.batch = it },
            label           = { Text(stringResource(R.string.journal_batch)) },
            singleLine      = true,
            modifier        = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value           = vm.airPressure,
            onValueChange   = { vm.airPressure = it },
            label           = { Text(stringResource(R.string.journal_air_pressure)) },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier        = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // ── Physical & Mental ratings ──────────────────────────────────────
        SectionLabel(stringResource(R.string.journal_state_title))

        EmojiRatingRow(
            label    = stringResource(R.string.journal_muscle_recovery),
            rating   = vm.muscleRecovery,
            onChange = { vm.muscleRecovery = it }
        )
        EmojiRatingRow(
            label    = stringResource(R.string.journal_fatigue),
            rating   = vm.fatigue,
            onChange = { vm.fatigue = it }
        )
        EmojiRatingRow(
            label    = stringResource(R.string.journal_concentration),
            rating   = vm.concentration,
            onChange = { vm.concentration = it }
        )
        EmojiRatingRow(
            label    = stringResource(R.string.journal_endurance),
            rating   = vm.endurance,
            onChange = { vm.endurance = it }
        )
        EmojiRatingRow(
            label    = stringResource(R.string.journal_heart_rate),
            rating   = vm.heartRate,
            onChange = { vm.heartRate = it }
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ── Reusable sub-composables ────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ClickableRow(
    icon   : androidx.compose.ui.graphics.vector.ImageVector,
    text   : String,
    onClick: () -> Unit
) {
    Row(
        modifier  = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StepperRow(
    label      : String,
    value      : Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    minValue   : Int = 1
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick  = onDecrement,
            enabled  = value > minValue
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = null,
                tint = if (value > minValue) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text       = value.toString(),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 12.dp)
        )
        IconButton(onClick = onIncrement) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmojiRatingRow(
    label   : String,
    rating  : Int,
    onChange: (Int) -> Unit
) {
    val emojis = listOf("😫", "😔", "😐", "🙂", "😄")

    Column {
        Text(
            text       = label,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            emojis.forEachIndexed { idx, emoji ->
                val emojiRating  = idx + 1
                val isSelected   = rating == emojiRating
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width  = if (isSelected) 2.dp else 0.dp,
                            color  = if (isSelected) MaterialTheme.colorScheme.primary
                                     else Color.Transparent,
                            shape  = CircleShape
                        )
                        .clickable {
                            // Tap again to deselect
                            onChange(if (rating == emojiRating) 0 else emojiRating)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

// ── TimePicker dialog wrapper ───────────────────────────────────────────────

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content  : @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape  = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}
