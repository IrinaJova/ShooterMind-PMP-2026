package com.shootermind.app.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.core.analytics.AnalyticsHelper
import com.shootermind.app.data.repository.SessionRepository
import com.shootermind.app.data.repository.SessionRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepo = SessionRepositoryImpl(
        ShooterMindDatabase.getDatabase(application).trainingSessionDao()
    )
    private val repository: SessionRepository = sessionRepo

    private val userId: String
        get() = Firebase.auth.currentUser?.uid ?: "anonymous"

    init {
        // Pull cloud sessions into Room when ViewModel starts
        viewModelScope.launch {
            if (userId != "anonymous") sessionRepo.syncFromCloud(userId)
        }
    }

    val sessions: StateFlow<List<TrainingSession>> = repository
        .getAllSessions(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentSessions: StateFlow<List<TrainingSession>> = repository
        .getRecentSessions(userId, 5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addSession(discipline: Discipline, totalScore: Double, notes: String) {
        viewModelScope.launch {
            repository.insertSession(
                TrainingSession(
                    id         = UUID.randomUUID().toString(),
                    userId     = userId,
                    discipline = discipline,
                    dateMs     = System.currentTimeMillis(),
                    totalScore = totalScore,
                    shotCount  = 0,
                    notes      = notes
                )
            )
            AnalyticsHelper.logSessionCreated(
                discipline = discipline.name,
                score      = totalScore
            )
        }
    }

    fun deleteSession(session: TrainingSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            AnalyticsHelper.logSessionDeleted(discipline = session.discipline.name)
        }
    }
}
