package com.shootermind.app.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.core.analytics.AnalyticsHelper
import com.shootermind.app.data.repository.SessionRepository
import com.shootermind.app.data.repository.SessionRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepo = SessionRepositoryImpl(
        ShooterMindDatabase.getDatabase(application).trainingSessionDao()
    )
    private val repository: SessionRepository = sessionRepo

    // Reacts to auth state changes — same pattern as ProfileViewModel.
    // Using a static `get()` property + stateIn evaluated at init time locks the
    // query to whatever uid was current when the ViewModel was first created,
    // which is wrong for multi-user / re-login scenarios.
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

    init {
        // Pull cloud sessions into Room whenever the signed-in user changes.
        viewModelScope.launch {
            userIdFlow.collectLatest { uid ->
                if (uid.isNotEmpty()) sessionRepo.syncFromCloud(uid)
            }
        }
    }

    val sessions: StateFlow<List<TrainingSession>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else repository.getAllSessions(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentSessions: StateFlow<List<TrainingSession>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else repository.getRecentSessions(uid, 5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addSession(discipline: Discipline, totalScore: Double, notes: String) {
        viewModelScope.launch {
            repository.insertSession(
                TrainingSession(
                    id         = UUID.randomUUID().toString(),
                    userId     = userIdFlow.value,
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
