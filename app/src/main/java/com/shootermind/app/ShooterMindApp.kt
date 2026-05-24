package com.shootermind.app

import android.app.Application
import com.shootermind.app.core.notification.NotificationHelper
import com.shootermind.app.core.util.LocaleUtils
import com.shootermind.app.core.util.OnboardingPrefs

class ShooterMindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        // Apply saved language preference on every app start
        val savedLang = OnboardingPrefs.getSelectedLanguage(this)
        LocaleUtils.setLocale(savedLang)
    }
}
