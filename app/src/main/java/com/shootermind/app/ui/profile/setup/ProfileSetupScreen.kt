package com.shootermind.app.ui.profile.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shootermind.app.R
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.domain.model.calculateISSFCategory
import com.shootermind.app.ui.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    profileViewModel: ProfileViewModel,
    onSetupComplete : () -> Unit
) {
    val context = LocalContext.current

    var firstName         by remember { mutableStateOf("") }
    var lastName          by remember { mutableStateOf("") }
    var birthDateMs       by remember { mutableLongStateOf(0L) }
    var showDatePicker    by remember { mutableStateOf(false) }
    var selectedDiscipline by remember { mutableStateOf(Discipline.AIR_RIFLE) }
    var personalBestText  by remember { mutableStateOf("") }
    var selectedGoal      by remember { mutableStateOf(TrainingGoal.IMPROVE_SCORE) }
    var goalDropdownOpen  by remember { mutableStateOf(false) }
    var profilePicPath    by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { profilePicPath = profileViewModel.copyImageToInternal(context, it) }
    }

    val dateLabel = if (birthDateMs > 0)
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(birthDateMs))
    else stringResource(R.string.label_date_of_birth)

    val issfLabel = if (birthDateMs > 0) {
        when (calculateISSFCategory(birthDateMs)) {
            com.shootermind.app.domain.model.ISSFCategory.YOUTH  -> stringResource(R.string.issf_youth)
            com.shootermind.app.domain.model.ISSFCategory.JUNIOR -> stringResource(R.string.issf_junior)
            com.shootermind.app.domain.model.ISSFCategory.SENIOR -> stringResource(R.string.issf_senior)
        }
    } else ""

    // ── Date picker dialog ─────────────────────────────────────────────────
    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = if (birthDateMs > 0) birthDateMs else null
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { birthDateMs = it }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        Text(
            text       = stringResource(R.string.screen_profile_setup),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = stringResource(R.string.profile_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        // ── Profile picture ────────────────────────────────────────────────
        Box(
            modifier        = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profilePicPath != null) {
                AsyncImage(
                    model            = profilePicPath,
                    contentDescription = "Profile picture",
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint     = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // ── Name fields ────────────────────────────────────────────────────
        OutlinedTextField(
            value         = firstName,
            onValueChange = { firstName = it },
            label         = { Text(stringResource(R.string.label_first_name)) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value         = lastName,
            onValueChange = { lastName = it },
            label         = { Text(stringResource(R.string.label_last_name)) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // ── Date of birth ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value         = dateLabel,
                onValueChange = {},
                label         = { Text(stringResource(R.string.label_date_of_birth)) },
                enabled       = false,
                trailingIcon  = if (issfLabel.isNotBlank()) ({
                    Text(
                        text     = issfLabel,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }) else null,
                colors        = OutlinedTextFieldDefaults.colors(
                    disabledTextColor        = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor      = MaterialTheme.colorScheme.outline,
                    disabledLabelColor       = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier      = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(20.dp))

        // ── Discipline ─────────────────────────────────────────────────────
        Text(
            text       = stringResource(R.string.new_session_discipline),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(4.dp))
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
        Spacer(Modifier.height(16.dp))

        // ── Personal best ──────────────────────────────────────────────────
        OutlinedTextField(
            value           = personalBestText,
            onValueChange   = { personalBestText = it },
            label           = { Text(stringResource(R.string.label_personal_best)) },
            placeholder     = { Text("0.0") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier        = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // ── Training goal dropdown ─────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded        = goalDropdownOpen,
            onExpandedChange = { goalDropdownOpen = it }
        ) {
            OutlinedTextField(
                value         = goalLabel(selectedGoal),
                onValueChange = {},
                readOnly      = true,
                label         = { Text(stringResource(R.string.label_goal)) },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalDropdownOpen) },
                modifier      = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded        = goalDropdownOpen,
                onDismissRequest = { goalDropdownOpen = false }
            ) {
                TrainingGoal.entries.forEach { goal ->
                    DropdownMenuItem(
                        text    = { Text(goalLabel(goal)) },
                        onClick = { selectedGoal = goal; goalDropdownOpen = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))

        // ── Save ───────────────────────────────────────────────────────────
        Button(
            onClick  = {
                profileViewModel.saveProfile(
                    firstName         = firstName.trim(),
                    lastName          = lastName.trim(),
                    birthDateMs       = if (birthDateMs > 0) birthDateMs
                                        else System.currentTimeMillis() - 20L * 365 * 24 * 3600 * 1000,
                    discipline        = selectedDiscipline,
                    personalBest      = personalBestText.toDoubleOrNull() ?: 0.0,
                    goal              = selectedGoal,
                    profilePictureUri = profilePicPath,
                    isNewProfile      = true
                )
                onSetupComplete()
            },
            enabled  = firstName.isNotBlank() && lastName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_save_profile))
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onSetupComplete) {
            Text(stringResource(R.string.btn_skip_for_now))
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
