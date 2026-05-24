package com.shootermind.app.ui.session

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.data.repository.SessionRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class NewSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val db          = ShooterMindDatabase.getDatabase(application)
    private val sessionRepo = SessionRepositoryImpl(db.trainingSessionDao())

    // ── Discipline ────────────────────────────────────────────────────────
    var defaultDiscipline by mutableStateOf(Discipline.AIR_RIFLE)
        private set
    var discipline by mutableStateOf(Discipline.AIR_RIFLE)

    // ── Date & time ───────────────────────────────────────────────────────
    var dateMs      by mutableLongStateOf(System.currentTimeMillis())
    var startHour   by mutableIntStateOf(-1)     // -1 = not set
    var startMinute by mutableIntStateOf(0)
    var endHour     by mutableIntStateOf(-1)
    var endMinute   by mutableIntStateOf(0)

    // ── Session type ──────────────────────────────────────────────────────
    var isCompetition    by mutableStateOf(false)
    var isControlSession by mutableStateOf(false)

    // ── Result ────────────────────────────────────────────────────────────
    var useDecimalScore  by mutableStateOf(true)
    var splitIntoSeries  by mutableStateOf(true)
    var seriesCount      by mutableIntStateOf(6)
    var shotsPerSeries   by mutableIntStateOf(10)

    // One text field per series (resizes with seriesCount)
    val seriesScores: MutableList<String> = mutableStateListOf<String>().also { list ->
        repeat(6) { list.add("") }
    }

    // Direct input (when splitIntoSeries = false)
    var totalResultText by mutableStateOf("")
    var shotCountText   by mutableStateOf("")

    // ── Journal ───────────────────────────────────────────────────────────
    var notes          by mutableStateOf("")
    var batch          by mutableStateOf("")
    var airPressure    by mutableStateOf("")
    var muscleRecovery by mutableIntStateOf(0)   // 0 = unset, 1-5
    var fatigue        by mutableIntStateOf(0)
    var concentration  by mutableIntStateOf(0)
    var endurance      by mutableIntStateOf(0)
    var heartRate      by mutableIntStateOf(0)

    // ── Error state ───────────────────────────────────────────────────────
    var saveError by mutableStateOf<String?>(null)

    // ── Computed ──────────────────────────────────────────────────────────
    val computedTotal: Double
        get() = if (splitIntoSeries)
            seriesScores.mapNotNull { it.toDoubleOrNull() }.sum()
        else
            totalResultText.toDoubleOrNull() ?: 0.0

    init { loadDefaultDiscipline() }

    private fun loadDefaultDiscipline() {
        viewModelScope.launch {
            val uid = Firebase.auth.currentUser?.uid ?: return@launch
            db.userProfileDao().getProfile(uid).firstOrNull()?.let { entity ->
                val d = runCatching { Discipline.valueOf(entity.discipline) }
                    .getOrDefault(Discipline.AIR_RIFLE)
                defaultDiscipline = d
                discipline        = d
            }
        }
    }

    // ── Stepper helpers ───────────────────────────────────────────────────

    fun updateSeriesCount(count: Int) {
        seriesCount = count.coerceIn(1, 20)
        while (seriesScores.size < seriesCount) seriesScores.add("")
        while (seriesScores.size > seriesCount) seriesScores.removeAt(seriesScores.lastIndex)
    }

    fun updateShotsPerSeries(count: Int) {
        shotsPerSeries = count.coerceIn(1, 60)
    }

    fun updateSeriesScore(index: Int, value: String) {
        if (index in seriesScores.indices) seriesScores[index] = value
    }

    // ── Save ──────────────────────────────────────────────────────────────

    fun saveSession(onDone: () -> Unit, onError: (String) -> Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        // Validation: need at least a result or some journal data
        val hasResult = when {
            splitIntoSeries -> seriesScores.any { it.toDoubleOrNull() != null }
            else            -> totalResultText.toDoubleOrNull() != null
        }
        val hasJournal = notes.isNotBlank() || batch.isNotBlank() ||
            airPressure.isNotBlank() || muscleRecovery > 0 ||
            fatigue > 0 || concentration > 0 || endurance > 0 || heartRate > 0

        if (!hasResult && !hasJournal) {
            saveError = "not_enough_data"
            onError("not_enough_data")
            return
        }
        saveError = null

        // Build series data and totals
        val (total, shots, seriesStr) = buildResultData()

        viewModelScope.launch {
            sessionRepo.insertSession(
                TrainingSession(
                    id               = UUID.randomUUID().toString(),
                    userId           = uid,
                    discipline       = discipline,
                    dateMs           = dateMs,
                    totalScore       = total,
                    shotCount        = shots,
                    notes            = notes.trim(),
                    seriesData       = seriesStr,
                    durationMinutes  = calculateDurationMinutes(),
                    photoUri         = null,
                    startHour        = startHour,
                    startMinute      = startMinute,
                    endHour          = endHour,
                    endMinute        = endMinute,
                    isCompetition    = isCompetition,
                    isControlSession = isControlSession,
                    useDecimalScore  = useDecimalScore,
                    splitIntoSeries  = splitIntoSeries,
                    seriesCount      = seriesCount,
                    shotsPerSeries   = shotsPerSeries,
                    batch            = batch.trim(),
                    airPressure      = airPressure.trim(),
                    muscleRecovery   = muscleRecovery,
                    fatigue          = fatigue,
                    concentration    = concentration,
                    endurance        = endurance,
                    heartRate        = heartRate
                )
            )
            onDone()
        }
    }

    private fun buildResultData(): Triple<Double, Int, String> {
        return if (splitIntoSeries) {
            val filled = seriesScores.mapNotNull { it.toDoubleOrNull() }
            val total  = filled.sum()
            val shots  = filled.size * shotsPerSeries
            val str    = filled.joinToString("|") { it.toString() }
            Triple(total, shots, str)
        } else {
            val total = totalResultText.toDoubleOrNull() ?: 0.0
            val shots = shotCountText.toIntOrNull() ?: 0
            Triple(total, shots, "")
        }
    }

    private fun calculateDurationMinutes(): Int {
        if (startHour < 0 || endHour < 0) return 0
        val startTotal = startHour * 60 + startMinute
        val endTotal   = endHour   * 60 + endMinute
        return (endTotal - startTotal).coerceAtLeast(0)
    }
}
