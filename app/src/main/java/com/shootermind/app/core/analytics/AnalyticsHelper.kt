package com.shootermind.app.core.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

/**
 * Thin wrapper around Firebase Analytics.
 * All event names follow Firebase naming rules (snake_case, ≤40 chars).
 */
object AnalyticsHelper {

    // ── Auth events ───────────────────────────────────────────────────────

    /** Call after a successful login (any method). */
    fun logLogin(method: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    /** Call after a successful new registration. */
    fun logSignUp(method: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    /** Call when the user signs out. */
    fun logSignOut() {
        Firebase.analytics.logEvent("sign_out") {}
    }

    // ── Session events ────────────────────────────────────────────────────

    /** Call after a training session is saved. */
    fun logSessionCreated(discipline: String, score: Double) {
        Firebase.analytics.logEvent("session_created") {
            param("discipline", discipline)
            param("score", score)
        }
    }

    /** Call after a training session is deleted. */
    fun logSessionDeleted(discipline: String) {
        Firebase.analytics.logEvent("session_deleted") {
            param("discipline", discipline)
        }
    }

    // ── Profile events ────────────────────────────────────────────────────

    /** Call when the user completes profile setup for the first time. */
    fun logProfileSetupComplete(category: String, discipline: String) {
        Firebase.analytics.logEvent("profile_setup_complete") {
            param("issf_category", category)
            param("discipline", discipline)
        }
    }

    /** Call when the user updates their existing profile. */
    fun logProfileUpdated() {
        Firebase.analytics.logEvent("profile_updated") {}
    }

    // ── Screen events ─────────────────────────────────────────────────────

    /** Manual screen-view tracking for screens that need it. */
    fun logScreenView(screenName: String) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
    }
}
