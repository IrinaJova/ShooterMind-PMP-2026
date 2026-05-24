package com.shootermind.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.data.repository.SessionRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsData(
    val totalSessions : Int    = 0,
    val averageScore  : Double = 0.0,
    val bestScore     : Double = 0.0,
    val rifleCount    : Int    = 0,
    val pistolCount   : Int    = 0,
    val rifleAvg      : Double = 0.0,
    val pistolAvg     : Double = 0.0,
    val recentScores  : List<Double> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepositoryImpl(
        ShooterMindDatabase.getDatabase(application).trainingSessionDao()
    )

    private val userId get() = Firebase.auth.currentUser?.uid ?: "anonymous"

    val stats: StateFlow<StatsData> = repository
        .getAllSessions(userId)
        .map { sessions -> sessions.toStatsData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsData())

    private fun List<TrainingSession>.toStatsData(): StatsData {
        if (isEmpty()) return StatsData()
        val rifle  = filter { it.discipline == Discipline.AIR_RIFLE }
        val pistol = filter { it.discipline == Discipline.AIR_PISTOL }
        return StatsData(
            totalSessions = size,
            averageScore  = map { it.totalScore }.average(),
            bestScore     = maxOf { it.totalScore },
            rifleCount    = rifle.size,
            pistolCount   = pistol.size,
            rifleAvg      = if (rifle.isEmpty()) 0.0 else rifle.map { it.totalScore }.average(),
            pistolAvg     = if (pistol.isEmpty()) 0.0 else pistol.map { it.totalScore }.average(),
            recentScores  = takeLast(10).map { it.totalScore }
        )
    }
}
