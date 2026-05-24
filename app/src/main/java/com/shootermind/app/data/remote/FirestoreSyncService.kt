package com.shootermind.app.data.remote

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.ISSFCategory
import com.shootermind.app.domain.model.TrainingGoal
import com.shootermind.app.domain.model.TrainingSession
import com.shootermind.app.domain.model.UserProfile
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper around Firestore.
 * Structure:
 *   users/{uid}/sessions/{sessionId}   — TrainingSession
 *   users/{uid}/profile                — UserProfile (single document)
 */
class FirestoreSyncService {

    private val db = Firebase.firestore

    // ── Sessions ───────────────────────────────────────────────────────────

    suspend fun upsertSession(session: TrainingSession) {
        db.collection("users")
            .document(session.userId)
            .collection("sessions")
            .document(session.id)
            .set(session.toMap())
            .await()
    }

    suspend fun deleteSession(session: TrainingSession) {
        db.collection("users")
            .document(session.userId)
            .collection("sessions")
            .document(session.id)
            .delete()
            .await()
    }

    suspend fun fetchSessions(userId: String): List<TrainingSession> {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toSession(userId) }
    }

    // ── Profile ────────────────────────────────────────────────────────────

    suspend fun upsertProfile(profile: UserProfile) {
        db.collection("users")
            .document(profile.userId)
            .collection("profile")
            .document("data")
            .set(profile.toMap())
            .await()
    }

    suspend fun fetchProfile(userId: String): UserProfile? {
        val doc = db.collection("users")
            .document(userId)
            .collection("profile")
            .document("data")
            .get()
            .await()
        return if (doc.exists()) doc.toProfile(userId) else null
    }

    // ── Mapping helpers ────────────────────────────────────────────────────

    private fun TrainingSession.toMap() = mapOf(
        "id"          to id,
        "userId"      to userId,
        "discipline"  to discipline.name,
        "dateMs"      to dateMs,
        "totalScore"  to totalScore,
        "shotCount"   to shotCount,
        "notes"       to notes
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toSession(userId: String): TrainingSession? {
        return try {
            TrainingSession(
                id          = getString("id") ?: id,
                userId      = userId,
                discipline  = Discipline.valueOf(getString("discipline") ?: "AIR_RIFLE"),
                dateMs      = getLong("dateMs") ?: 0L,
                totalScore  = getDouble("totalScore") ?: 0.0,
                shotCount   = getLong("shotCount")?.toInt() ?: 0,
                notes       = getString("notes") ?: ""
            )
        } catch (e: Exception) { null }
    }

    private fun UserProfile.toMap() = mapOf(
        "userId"            to userId,
        "firstName"         to firstName,
        "lastName"          to lastName,
        "birthDateMs"       to birthDateMs,
        "issfCategory"      to issfCategory.name,
        "discipline"        to discipline.name,
        "personalBest"      to personalBest,
        "goal"              to goal.name,
        "profilePictureUri" to (profilePictureUri ?: "")
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toProfile(userId: String): UserProfile? {
        return try {
            UserProfile(
                userId            = userId,
                firstName         = getString("firstName") ?: "",
                lastName          = getString("lastName") ?: "",
                birthDateMs       = getLong("birthDateMs") ?: 0L,
                issfCategory      = ISSFCategory.valueOf(getString("issfCategory") ?: "SENIOR"),
                discipline        = Discipline.valueOf(getString("discipline") ?: "AIR_RIFLE"),
                personalBest      = getDouble("personalBest") ?: 0.0,
                goal              = TrainingGoal.valueOf(getString("goal") ?: "IMPROVE_SCORE"),
                profilePictureUri = getString("profilePictureUri")?.ifBlank { null }
            )
        } catch (e: Exception) { null }
    }
}
