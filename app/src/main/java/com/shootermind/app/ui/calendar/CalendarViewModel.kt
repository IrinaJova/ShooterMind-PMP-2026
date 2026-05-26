package com.shootermind.app.ui.calendar

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.shootermind.app.core.notification.EventReminderReceiver
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.data.repository.CalendarRepository
import com.shootermind.app.data.repository.CalendarRepositoryImpl
import com.shootermind.app.domain.model.CalendarEvent
import com.shootermind.app.domain.model.EventType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository = CalendarRepositoryImpl(
        ShooterMindDatabase.getDatabase(application).calendarEventDao()
    )

    // Auth-reactive uid — same pattern as SessionViewModel / ProfileViewModel.
    // Evaluating userId once at init locks the Room query to the wrong user
    // after a re-login (ViewModel is activity-scoped, persists across logins).
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

    // userId used by write operations — reads current auth at call time, correct.
    private val userId get() = Firebase.auth.currentUser?.uid ?: "anonymous"

    val allEvents: StateFlow<List<CalendarEvent>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else repository.getAllEvents(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val upcomingEvents: StateFlow<List<CalendarEvent>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else repository.getUpcomingEvents(uid, System.currentTimeMillis(), 5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveEvent(event: CalendarEvent) {
        viewModelScope.launch {
            val toSave = if (event.id.isBlank()) event.copy(
                id        = UUID.randomUUID().toString(),
                userId    = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ) else event.copy(updatedAt = System.currentTimeMillis())

            repository.saveEvent(toSave)

            // Cancel any existing alarm, then (re)schedule if reminder is on
            cancelReminder(toSave.id)
            if (toSave.reminderEnabled) scheduleReminder(toSave)
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            cancelReminder(event.id)
            repository.deleteEvent(event.id)
        }
    }

    // ── AlarmManager ────────────────────────────────────────────────────────

    private fun scheduleReminder(event: CalendarEvent) {
        val reminderMs = event.dateTimeMs - event.reminderMinsBefore * 60_000L
        if (reminderMs <= System.currentTimeMillis()) return

        val ctx = getApplication<Application>()
        val am  = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(ctx, EventReminderReceiver::class.java).apply {
            putExtra(EventReminderReceiver.EXTRA_TITLE,        event.title)
            putExtra(EventReminderReceiver.EXTRA_TYPE,         event.eventType.name)
            putExtra(EventReminderReceiver.EXTRA_REMINDER_MINS, event.reminderMinsBefore)
        }
        val pi = PendingIntent.getBroadcast(
            ctx,
            abs(event.id.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setAlarmClock requires USE_EXACT_ALARM (declared in manifest, normal
        // permission, auto-granted).  Wrap in try-catch so that an edge-case
        // SecurityException on older/custom ROMs never crashes the save flow.
        try {
            val alarmInfo = AlarmManager.AlarmClockInfo(reminderMs, pi)
            am.setAlarmClock(alarmInfo, pi)
        } catch (e: SecurityException) {
            Log.e("CalendarVM", "setAlarmClock denied — USE_EXACT_ALARM not granted: $e")
        }
    }

    private fun cancelReminder(eventId: String) {
        val ctx = getApplication<Application>()
        val am  = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, EventReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx,
            abs(eventId.hashCode()),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        am.cancel(pi)
    }

    /** Build a new blank event with sensible defaults */
    fun newEvent(): CalendarEvent = CalendarEvent(
        id                 = "",
        userId             = userId,
        title              = "",
        eventType          = EventType.TRAINING,
        dateTimeMs         = System.currentTimeMillis() + 24 * 3600_000L,
        location           = "",
        discipline         = null,
        goalScore          = 0.0,
        notes              = "",
        reminderEnabled    = true,
        reminderMinsBefore = 60,
        createdAt          = 0L,
        updatedAt          = 0L
    )
}
