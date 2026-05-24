package com.shootermind.app.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.shootermind.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application
        .getSharedPreferences("shootermind_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    private fun loadTheme(): ThemeMode {
        val saved = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return ThemeMode.entries.find { it.name == saved } ?: ThemeMode.SYSTEM
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
    }
}
