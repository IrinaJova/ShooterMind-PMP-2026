package com.shootermind.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.data.repository.SessionRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class StatsData(
    val totalSessions       : Int                    = 0,
    val averageScore        : Double                 = 0.0,
    val bestScore           : Double                 = 0.0,
    val rifleCount          : Int                    = 0,
    val pistolCount         : Int                    = 0,
    val rifleAvg            : Double                 = 0.0,
    val pistolAvg           : Double                 = 0.0,
    val recentScores        : List<Double>           = emptyList(),
    // Extended
    val weeklySessionCount  : Int                    = 0,
    val weeklyAverageScore  : Double                 = 0.0,
    val totalTrainingMinutes: Int                    = 0,
    val competitionCount    : Int                    = 0,
    val streak              : Int                    = 0,
    // Chart — last 20 sessions sorted by date: (dateMs, totalScore)
    val chartPoints         : List<Pair<Long,Double>> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepositoryImpl(
        ShooterMindDatabase.getDatabase(application).trainingSessionDao()
    )

    // Same auth-reactive pattern as SessionViewModel / ProfileViewModel:
    // evaluating userId once at init locks the query to the wrong user after re-login.
    private val userIdFlow: StateFlow<String> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid ?: "")
        }
        Firebase.auth.addAuthStateListener(listener)
        awaitClose { Firebase.auth.removeAuthStateListener(listener) }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.Eagerly,
        initialValue = Firebase.auth.currentUser?.uid ?: ""
    )

    val stats: StateFlow<StatsData> = userIdFlow
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else repository.getAllSessions(uid)
        }
        .map { sessions -> sessions.toStatsData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsData())

    private fun List<TrainingSession>.toStatsData(): StatsData {
        if (isEmpty()) return StatsData()

        val rifle   = filter { it.discipline == Discipline.AIR_RIFLE }
        val pistol  = filter { it.discipline == Discipline.AIR_PISTOL }
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val weekly  = filter { it.dateMs >= weekAgo }

        return StatsData(
            totalSessions        = size,
            averageScore         = map { it.totalScore }.average(),
            bestScore            = maxOf { it.totalScore },
            rifleCount           = rifle.size,
            pistolCount          = pistol.size,
            rifleAvg             = if (rifle.isEmpty()) 0.0 else rifle.map { it.totalScore }.average(),
            pistolAvg            = if (pistol.isEmpty()) 0.0 else pistol.map { it.totalScore }.average(),
            recentScores         = takeLast(10).map { it.totalScore },
            weeklySessionCount   = weekly.size,
            weeklyAverageScore   = if (weekly.isEmpty()) 0.0 else weekly.map { it.totalScore }.average(),
            totalTrainingMinutes = sumOf { it.durationMinutes },
            competitionCount     = count { it.isCompetition },
            streak               = computeStreak(this),
            chartPoints          = this.sortedBy { it.dateMs }.takeLast(20)
                                       .map { it.dateMs to it.totalScore }
        )
    }

    private fun computeStreak(sessions: List<TrainingSession>): Int {
        if (sessions.isEmpty()) return 0
        val dayMs   = 24L * 60 * 60 * 1000
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var streak   = 0
        var checkDay = todayStart
        while (true) {
            val hasSession = sessions.any { it.dateMs in checkDay until checkDay + dayMs }
            if (!hasSession) break
            streak++
            checkDay -= dayMs
        }
        return streak
    }
}
