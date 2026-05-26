package com.shootermind.app.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shootermind.app.R
import com.shootermind.app.core.util.FileUtils
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingGoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Purple500 = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onNavigateBack  : () -> Unit
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val existing = (profileState as? ProfileState.Complete)?.profile
    val context  = LocalContext.current

    // ── Form state ────────────────────────────────────────────────────────
    var firstName    by remember { mutableStateOf(existing?.firstName     ?: "") }
    var lastName     by remember { mutableStateOf(existing?.lastName      ?: "") }
    var personalBest by remember { mutableStateOf(
        if ((existing?.personalBest ?: 0.0) > 0) "%.1f".format(existing?.personalBest) else ""
    )}
    var discipline   by remember { mutableStateOf(existing?.discipline    ?: Discipline.AIR_RIFLE) }
    var goal         by remember { mutableStateOf(existing?.goal          ?: TrainingGoal.IMPROVE_SCORE) }
    var birthDateMs  by remember { mutableLongStateOf(existing?.birthDateMs ?: 0L) }
    var photoPath    by remember { mutableStateOf(existing?.profilePictureUri) }

    var showDatePicker   by remember { mutableStateOf(false) }
    var showDiscipline   by remember { mutableStateOf(false) }
    var firstNameError   by remember { mutableStateOf(false) }
    var lastNameError    by remember { mutableStateOf(false) }

    val dateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    // ── Photo source dialog ────────────────────────────────────────────────
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var cameraFile            by remember { mutableStateOf<java.io.File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraFile?.absolutePath?.let { photoPath = it }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { photoPath = profileViewModel.copyImageToInternal(context, it) }
    }

    // Gallery permission differs by API level
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    // Permission launchers — must be declared unconditionally (Compose rule)
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, file) = FileUtils.createCameraImageUri(context)
            cameraFile = file
            cameraLauncher.launch(uri)
        }
    }

    val galleryPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) galleryLauncher.launch("image/*")
    }

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title            = { Text(stringResource(R.string.photo_source_title)) },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick  = {
                            showPhotoSourceDialog = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_GRANTED) {
                                val (uri, file) = FileUtils.createCameraImageUri(context)
                                cameraFile = file
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("📷  " + stringResource(R.string.photo_source_camera)) }
                    TextButton(
                        onClick  = {
                            showPhotoSourceDialog = false
                            if (ContextCompat.checkSelfPermission(context, galleryPermission)
                                    == PackageManager.PERMISSION_GRANTED) {
                                galleryLauncher.launch("image/*")
                            } else {
                                galleryPermLauncher.launch(galleryPermission)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🖼️  " + stringResource(R.string.photo_source_gallery)) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_edit_profile)) },
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
                .padding(24.dp)
        ) {

            // ── Profile photo ─────────────────────────────────────────────
            Box(
                modifier         = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier         = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, Purple500, CircleShape)
                        .clickable { showPhotoSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoPath != null) {
                        AsyncImage(
                            model              = photoPath,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint     = Purple500,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Purple500)
                        .clickable { showPhotoSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.profile_photo_change),
                        tint     = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            TextButton(
                onClick  = { showPhotoSourceDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.profile_photo_change))
            }

            Spacer(Modifier.height(8.dp))

            // ── Name fields ───────────────────────────────────────────────
            OutlinedTextField(
                value         = firstName,
                onValueChange = { firstName = it; firstNameError = false },
                label         = { Text(stringResource(R.string.label_first_name)) },
                isError       = firstNameError,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            if (firstNameError) {
                Text(
                    stringResource(R.string.error_field_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value         = lastName,
                onValueChange = { lastName = it; lastNameError = false },
                label         = { Text(stringResource(R.string.label_last_name)) },
                isError       = lastNameError,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
            if (lastNameError) {
                Text(
                    stringResource(R.string.error_field_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(8.dp))

            // Date of birth
            OutlinedTextField(
                value         = if (birthDateMs > 0) dateFmt.format(Date(birthDateMs)) else "",
                onValueChange = {},
                readOnly      = true,
                label         = { Text(stringResource(R.string.label_date_of_birth)) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                trailingIcon  = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(stringResource(R.string.calendar_event_date))
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            // Personal best
            OutlinedTextField(
                value         = personalBest,
                onValueChange = { personalBest = it },
                label         = { Text(stringResource(R.string.label_personal_best)) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Discipline
            Text(
                text       = stringResource(R.string.new_session_discipline),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded         = showDiscipline,
                onExpandedChange = { showDiscipline = it }
            ) {
                OutlinedTextField(
                    value         = when (discipline) {
                        Discipline.AIR_RIFLE  -> stringResource(R.string.discipline_air_rifle)
                        Discipline.AIR_PISTOL -> stringResource(R.string.discipline_air_pistol)
                    },
                    onValueChange = {},
                    readOnly      = true,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showDiscipline) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded         = showDiscipline,
                    onDismissRequest = { showDiscipline = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text(stringResource(R.string.discipline_air_rifle)) },
                        onClick = { discipline = Discipline.AIR_RIFLE; showDiscipline = false }
                    )
                    DropdownMenuItem(
                        text    = { Text(stringResource(R.string.discipline_air_pistol)) },
                        onClick = { discipline = Discipline.AIR_PISTOL; showDiscipline = false }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Goal
            Text(
                text       = stringResource(R.string.label_goal),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
            TrainingGoal.entries.forEach { g ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = goal == g,
                        onClick  = { goal = g }
                    )
                    Text(
                        text  = goalLabel(g),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = {
                    if (firstName.isBlank()) { firstNameError = true; return@Button }
                    if (lastName.isBlank())  { lastNameError  = true; return@Button }
                    profileViewModel.saveProfile(
                        firstName         = firstName.trim(),
                        lastName          = lastName.trim(),
                        birthDateMs       = if (birthDateMs > 0) birthDateMs
                                            else existing?.birthDateMs ?: System.currentTimeMillis(),
                        discipline        = discipline,
                        personalBest      = personalBest.toDoubleOrNull() ?: existing?.personalBest ?: 0.0,
                        goal              = goal,
                        profilePictureUri = photoPath,
                        isNewProfile      = false
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.btn_save_changes),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // Date picker
    if (showDatePicker) {
        val initialMs = if (birthDateMs > 0) birthDateMs else
            System.currentTimeMillis() - 20L * 365 * 24 * 3600_000
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { birthDateMs = it }
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
}

@Composable
private fun goalLabel(g: TrainingGoal): String = when (g) {
    TrainingGoal.IMPROVE_SCORE    -> stringResource(R.string.goal_improve_score)
    TrainingGoal.CONSISTENCY      -> stringResource(R.string.goal_consistency)
    TrainingGoal.TECHNIQUE        -> stringResource(R.string.goal_technique)
    TrainingGoal.COMPETITION_PREP -> stringResource(R.string.goal_competition_prep)
    TrainingGoal.MENTAL_TRAINING  -> stringResource(R.string.goal_mental)
}
