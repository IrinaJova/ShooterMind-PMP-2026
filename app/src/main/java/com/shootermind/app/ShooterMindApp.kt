package com.shootermind.app

import android.app.Application
import com.shootermind.app.core.notification.NotificationHelper

class ShooterMindApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
