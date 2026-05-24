package com.shootermind.app.core.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleUtils {

    fun setLocale(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    fun currentLanguageTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) "en"
        else locales[0]?.toLanguageTag() ?: "en"
    }

    fun isMacedonian(): Boolean = currentLanguageTag().startsWith("mk")
}
