package com.shootermind.app.ui.calendar.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.domain.model.CalendarEvent
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.EventType
import com.shootermind.app.domain.model.ReminderOffset
import com.shootermind.app.ui.calendar.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    eventId          : String?,           // null = new event
    onNavigateBack   : () -> Unit,
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val allEvents by calendarViewModel.allEvents.collectAsState()
    val existing   = if (!eventId.isNullOrBlank()) allEvents.find { it.id == eventId } else null

    // ── Form state ───────────────────────────────────────────────────────────
    var title       by remember { mutableStateOf(existing?.title              ?: "") }
    var eventType   by remember { mutableStateOf(existing?.eventType          ?: EventType.TRAINING) }
    var selectedMs  by remember { mutableStateOf(existing?.dateTimeMs         ?: (System.currentTimeMillis() + 24 * 3600_000L)) }
    var location    by remember { mutableStateOf(existing?.location           ?: "") }
    var discipline  by remember { mutableStateOf(existing?.discipline) }
    var goalScore   by remember { mutableStateOf(if ((existing?.goalScore ?: 0.0) > 0) "%.1f".format(existing?.goalScore) else "") }
    var notes       by remember { mutableStateOf(existing?.notes              ?: "") }
    var reminderOn  by remember { mutableStateOf(existing?.reminderEnabled    ?: true) }
    var reminderMin by remember { mutableStateOf(existing?.reminderMinsBefore ?: ReminderOffset.HOUR_1) }

    // ── Dialog visibility ────────────────────────────────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTypeMenu   by remember { mutableStateOf(false) }
    var showDiscMenu   by remember { mutableStateOf(false) }
    var showRemMenu    by remember { mutableStateOf(false) }
    var titleError     by remember { mutableStateOf(false) }

    val dateFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    val timeFmt = SimpleDateFormat("HH:mm",           Locale.getDefault())

    val screenTitle = if (existing != null)
        stringResource(R.string.calendar_edit_event)
    else
        stringResource(R.string.calendar_add_event)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Title
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it; titleError = false },
                label         = { Text(stringResource(R.string.calendar_event_title)) },
                isError       = titleError,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            if (titleError) {
                Text(
                    text  = stringResource(R.string.error_field_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Event type dropdown
            ExposedDropdownMenuBox(
                expanded         = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value         = eventTypeLabel(eventType),
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text(stringResource(R.string.calendar_event_type)) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeMenu) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded         = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    EventType.entries.forEach { type ->
                        DropdownMenuItem(
                            text    = { Text(eventTypeLabel(type)) },
                            onClick = { eventType = type; showTypeMenu = false }
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value         = dateFmt.format(Date(selectedMs)),
                onValueChange = {},
                readOnly      = true,
                label         = { Text(stringResource(R.string.calendar_event_date)) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .then(Modifier.noRippleClickable { showDatePicker = true }),
                trailingIcon  = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(stringResource(R.string.calendar_event_date))
                    }
                }
            )

            // Time
            OutlinedTextField(
                value         = timeFmt.format(Date(selectedMs)),
                onValueChange = {},
                readOnly      = true,
                label         = { Text(stringResource(R.string.calendar_event_time)) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .then(Modifier.noRippleClickable { showTimePicker = true }),
                trailingIcon  = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(stringResource(R.string.calendar_event_time))
                    }
                }
            )

            // Location
            OutlinedTextField(
                value         = location,
                onValueChange = { location = it },
                label         = { Text(stringResource(R.string.calendar_event_location)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Discipline (optional)
            ExposedDropdownMenuBox(
                expanded         = showDiscMenu,
                onExpandedChange = { showDiscMenu = it }
            ) {
                OutlinedTextField(
                    value         = discipline?.let { disciplineLabel(it) }
                                    ?: stringResource(R.string.calendar_event_discipline),
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text(stringResource(R.string.calendar_event_discipline)) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showDiscMenu) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded         = showDiscMenu,
                    onDismissRequest = { showDiscMenu = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("—") },
                        onClick = { discipline = null; showDiscMenu = false }
                    )
                    Discipline.entries.forEach { d ->
                        DropdownMenuItem(
                            text    = { Text(disciplineLabel(d)) },
                            onClick = { discipline = d; showDiscMenu = false }
                        )
                    }
                }
            }

            // Goal score
            OutlinedTextField(
                value         = goalScore,
                onValueChange = { goalScore = it },
                label         = { Text(stringResource(R.string.calendar_event_goal)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier      = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text(stringResource(R.string.calendar_event_notes)) },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Reminder ──────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.calendar_reminder_section))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(R.string.calendar_reminder_enabled),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked         = reminderOn,
                    onCheckedChange = { reminderOn = it }
                )
            }

            if (reminderOn) {
                ExposedDropdownMenuBox(
                    expanded         = showRemMenu,
                    onExpandedChange = { showRemMenu = it }
                ) {
                    OutlinedTextField(
                        value         = reminderLabel(reminderMin),
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text(stringResource(R.string.calendar_reminder_when)) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showRemMenu) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded         = showRemMenu,
                        onDismissRequest = { showRemMenu = false }
                    ) {
                        listOf(
                            ReminderOffset.MINS_30 to R.string.calendar_reminder_30min,
                            ReminderOffset.HOUR_1  to R.string.calendar_reminder_1hr,
                            ReminderOffset.HOURS_2 to R.string.calendar_reminder_2hrs,
                            ReminderOffset.DAY_1   to R.string.calendar_reminder_1day
                        ).forEach { (mins, labelRes) ->
                            DropdownMenuItem(
                                text    = { Text(stringResource(labelRes)) },
                                onClick = { reminderMin = mins; showRemMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick  = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedMs }
                    val event = CalendarEvent(
                        id                 = existing?.id ?: "",
                        userId             = existing?.userId ?: "",
                        title              = title.trim(),
                        eventType          = eventType,
                        dateTimeMs         = cal.timeInMillis,
                        location           = location.trim(),
                        discipline         = discipline,
                        goalScore          = goalScore.toDoubleOrNull() ?: 0.0,
                        notes              = notes.trim(),
                        reminderEnabled    = reminderOn,
                        reminderMinsBefore = reminderMin,
                        createdAt          = existing?.createdAt ?: 0L,
                        updatedAt          = 0L
                    )
                    calendarViewModel.saveEvent(event)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = stringResource(R.string.btn_save),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Date Picker Dialog ───────────────────────────────────────────────────
    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { pickedDay ->
                        val calOld = Calendar.getInstance().apply { timeInMillis = selectedMs }
                        val calNew = Calendar.getInstance().apply { timeInMillis = pickedDay }
                        calNew.set(Calendar.HOUR_OF_DAY, calOld.get(Calendar.HOUR_OF_DAY))
                        calNew.set(Calendar.MINUTE,      calOld.get(Calendar.MINUTE))
                        selectedMs = calNew.timeInMillis
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.btn_save)) }
            },
            dismissButton    = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) { DatePicker(state = dateState) }
    }

    // ── Time Picker Dialog ───────────────────────────────────────────────────
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedMs }
        val timeState = rememberTimePickerState(
            initialHour   = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour      = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = stringResource(R.string.calendar_event_time),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    TimePicker(state = timeState)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        TextButton(onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = selectedMs }
                            c.set(Calendar.HOUR_OF_DAY, timeState.hour)
                            c.set(Calendar.MINUTE, timeState.minute)
                            selectedMs = c.timeInMillis
                            showTimePicker = false
                        }) { Text(stringResource(R.string.btn_save)) }
                    }
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary,
        modifier   = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun eventTypeLabel(type: EventType): String = when (type) {
    EventType.TRAINING    -> stringResource(R.string.calendar_type_training)
    EventType.COMPETITION -> stringResource(R.string.calendar_type_competition)
    EventType.RECOVERY    -> stringResource(R.string.calendar_type_recovery)
    EventType.OTHER       -> stringResource(R.string.calendar_type_other)
}

@Composable
private fun disciplineLabel(d: Discipline): String = when (d) {
    Discipline.AIR_RIFLE  -> stringResource(R.string.discipline_air_rifle)
    Discipline.AIR_PISTOL -> stringResource(R.string.discipline_air_pistol)
}

@Composable
private fun reminderLabel(mins: Int): String = when (mins) {
    ReminderOffset.MINS_30 -> stringResource(R.string.calendar_reminder_30min)
    ReminderOffset.HOUR_1  -> stringResource(R.string.calendar_reminder_1hr)
    ReminderOffset.HOURS_2 -> stringResource(R.string.calendar_reminder_2hrs)
    ReminderOffset.DAY_1   -> stringResource(R.string.calendar_reminder_1day)
    else                   -> "$mins min"
}

/** Extension for read-only text fields that need a tap handler without Ripple issues */
private fun Modifier.noRippleClickable(onClick: () -> Unit) = this
