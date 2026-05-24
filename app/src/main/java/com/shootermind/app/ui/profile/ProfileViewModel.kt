package com.shootermind.app.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shootermind.app.data.local.ShooterMindDatabase
import com.shootermind.app.data.repository.UserProfileRepository
import com.shootermind.app.data.repository.UserProfileRepositoryImpl
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.ISSFCategory
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.domain.model.UserProfile
import com.shootermind.app.domain.model.calculateISSFCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    init {
        viewModelScope.launch {
            if (!isAnonymous && userId.isNotBlank()) profileRepo.syncFromCloud(userId)
        }
    }

    private val userId get() = Firebase.auth.currentUser?.uid ?: ""
    val isAnonymous   get() = Firebase.auth.currentUser?.isAnonymous == true

    val profileState: StateFlow<ProfileState> = repository
        .getProfile(userId)
        .map { profile ->
            if (profile == null) ProfileState.Empty
            else ProfileState.Complete(profile)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileState.Loading)

    fun saveProfile(
        firstName        : String,
        lastName         : String,
        birthDateMs      : Long,
        discipline       : Discipline,
        personalBest     : Double,
        goal             : TrainingGoal,
        profilePictureUri: String?
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
        }
    }

    /** Copies picked image to private storage and returns the path. */
    fun copyImageToInternal(context: Context, uri: Uri): String {
        val dir  = File(context.filesDir, "profile_pics").also { it.mkdirs() }
        val dest = File(dir, "$userId.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest.absolutePath
    }

    fun signOut() {
        Firebase.auth.signOut()
    }
}
