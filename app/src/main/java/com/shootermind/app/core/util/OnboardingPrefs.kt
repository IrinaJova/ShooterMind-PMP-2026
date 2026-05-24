package com.shootermind.app.core.util

import android.content.Context

object OnboardingPrefs {

    private const val PREFS_NAME    = "shootermind_prefs"
    private const val KEY_COMPLETED = "onboarding_completed"
    private const val KEY_LANGUAGE  = "onboarding_language"

    fun isCompleted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_COMPLETED, false)

    fun markCompleted(context: Context) {
        prefs(context).edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    fun getSelectedLanguage(context: Context): String =
        prefs(context).getString(KEY_LANGUAGE, "en") ?: "en"

    fun setLanguage(context: Context, languageTag: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, languageTag).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
