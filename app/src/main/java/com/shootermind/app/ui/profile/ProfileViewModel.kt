package com.shootermind.app.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.core.analytics.AnalyticsHelper
import com.shootermind.app.core.util.LocaleUtils
import com.shootermind.app.core.util.OnboardingPrefs
import com.shootermind.app.data.repository.UserProfileRepository
import com.shootermind.app.data.repository.UserProfileRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.ISSFCategory
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.domain.model.UserProfile
import com.shootermind.app.domain.model.calculateISSFCategory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface ProfileState {
    data object Loading  : ProfileState
    data object Empty    : ProfileState
    data class  Complete(val profile: UserProfile) : ProfileState
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db          = ShooterMindDatabase.getDatabase(application)
    private val profileRepo = UserProfileRepositoryImpl(db.userProfileDao())
    private val repository  : UserProfileRepository = profileRepo

    // ── Auth-reactive userId ───────────────────────────────────────────────
    // Emits the current user's UID whenever Firebase auth state changes.
    // This is the fix for the bug where ProfileViewModel was created before
    // login (userId = ""), causing Room to always query for the wrong UID.
    private val userIdFlow: StateFlow<String> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid ?: "")
        }
        Firebase.auth.addAuthStateListener(listener)
        awaitClose { Firebase.auth.removeAuthStateListener(listener) }
    }.stateIn(
        scope           = viewModelScope,
        started         = SharingStarted.Eagerly,
        initialValue    = Firebase.auth.currentUser?.uid ?: ""
    )

    private val userId  get() = Firebase.auth.currentUser?.uid ?: ""
    val isAnonymous     get() = Firebase.auth.currentUser?.isAnonymous == true

    // ── Sync progress ──────────────────────────────────────────────────────
    // True while syncFromCloud is running for the current user.
    // UI uses this to show a spinner instead of the setup form so the user
    // never sees an empty form while their Firestore profile is being fetched.
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // ── Profile state ──────────────────────────────────────────────────────
    // Reacts to auth state: when userId changes (login/logout/register),
    // the Room query is re-executed for the new UID.
    val profileState: StateFlow<ProfileState> = userIdFlow
        .flatMapLatest { uid ->
            Log.d("ProfileDebug", "profileState: uid changed → '$uid'")
            if (uid.isEmpty()) {
                flowOf(ProfileState.Empty)
            } else {
                repository.getProfile(uid).map { profile ->
                    val state = if (profile == null) ProfileState.Empty
                                else                 ProfileState.Complete(profile)
                    Log.d("ProfileDebug", "profileState: Room for uid='$uid' → " +
                        if (profile == null) "null → Empty"
                        else "profile(userId=${profile.userId}) → Complete")
                    state
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileState.Loading
        )

    // ── Cloud sync on login ────────────────────────────────────────────────
    // Uses collectLatest so that if the uid changes (user switches) while a
    // sync is in progress, the old sync is cancelled immediately and the new
    // user's sync starts.  The finally block guarantees _isSyncing is cleared
    // even if the coroutine is cancelled mid-flight.
    init {
        // ── Firestore sync ─────────────────────────────────────────────────
        viewModelScope.launch {
            userIdFlow.collectLatest { uid ->
                try {
                    if (uid.isNotBlank() && !(Firebase.auth.currentUser?.isAnonymous == true)) {
                        Log.d("ProfileDebug", "syncFromCloud: starting for uid='$uid'")
                        _isSyncing.value = true
                        profileRepo.syncFromCloud(uid)
                        Log.d("ProfileDebug", "syncFromCloud: done for uid='$uid'")
                    } else {
                        Log.d("ProfileDebug", "syncFromCloud: skipped (uid='$uid', " +
                            "isAnonymous=${Firebase.auth.currentUser?.isAnonymous})")
                    }
                } finally {
                    _isSyncing.value = false
                }
            }
        }

        // ── FCM token on login ─────────────────────────────────────────────
        // onNewToken() only fires when the token rotates, so if the user logs
        // in after the token was already issued the token would never reach
        // Firestore.  Re-fetch and save on every login to guarantee the Cloud
        // Function always has a valid token for this user.
        viewModelScope.launch {
            userIdFlow.collectLatest { uid ->
                if (uid.isBlank() || Firebase.auth.currentUser?.isAnonymous == true) return@collectLatest
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    Firebase.firestore
                        .collection("users")
                        .document(uid)
                        .collection("fcmTokens")
                        .document("current")
                        .set(mapOf(
                            "token"     to token,
                            "updatedAt" to System.currentTimeMillis()
                        ))
                }
            }
        }
    }

    // ── Save profile ───────────────────────────────────────────────────────
    /**
     * Saves the profile to Room and Firestore.
     * [onComplete] is invoked on the Main dispatcher after Room has persisted
     * the data — use it to navigate away from the setup screen so that
     * ProfileScreen always sees the saved data immediately.
     */
    fun saveProfile(
        firstName        : String,
        lastName         : String,
        birthDateMs      : Long,
        discipline       : Discipline,
        personalBest     : Double,
        goal             : TrainingGoal,
        profilePictureUri: String?,
        isNewProfile     : Boolean        = false,
        onComplete       : (() -> Unit)?  = null
    ) {
        viewModelScope.launch {
            val category = calculateISSFCategory(birthDateMs)
            repository.saveProfile(
                UserProfile(
                    userId            = userId,
                    firstName         = firstName,
                    lastName          = lastName,
                    birthDateMs       = birthDateMs,
                    issfCategory      = category,
                    discipline        = discipline,
                    personalBest      = personalBest,
                    goal              = goal,
                    profilePictureUri = profilePictureUri
                )
            )
            // Room has saved; now it's safe to navigate
            onComplete?.invoke()

            if (isNewProfile) {
                AnalyticsHelper.logProfileSetupComplete(
                    category   = category.name,
                    discipline = discipline.name
                )
            } else {
                AnalyticsHelper.logProfileUpdated()
            }
        }
    }

    /** Copies a picked gallery URI to private storage and returns the absolute path. */
    fun copyImageToInternal(context: Context, uri: Uri): String {
        val dir  = File(context.filesDir, "profile_pics").also { it.mkdirs() }
        val dest = File(dir, "$userId.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest.absolutePath
    }

    // ── Sign out ───────────────────────────────────────────────────────────
    fun signOut() {
        Firebase.auth.signOut()
        // Reset language to English so that the next user (or the same user
        // after a fresh login) starts in English, not the previous user's choice.
        val app = getApplication<Application>()
        OnboardingPrefs.setLanguage(app, "en")
        LocaleUtils.setLocale("en")
    }
}
