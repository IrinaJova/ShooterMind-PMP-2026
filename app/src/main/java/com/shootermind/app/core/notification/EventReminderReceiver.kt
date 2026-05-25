package com.shootermind.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TITLE         = "event_title"
        const val EXTRA_TYPE          = "event_type"
        const val EXTRA_REMINDER_MINS = "reminder_mins"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title       = intent.getStringExtra(EXTRA_TITLE) ?: "Upcoming Event"
        val type        = intent.getStringExtra(EXTRA_TYPE)  ?: "TRAINING"
        val reminderMin = intent.getIntExtra(EXTRA_REMINDER_MINS, 60)

        val (notifTitle, notifBody) = when {
            type == "COMPETITION" && reminderMin >= 1440 ->
                Pair("Competition tomorrow: $title",
                     "Prepare your equipment and focus plan")
            type == "COMPETITION" && reminderMin >= 90 ->
                Pair("Competition in 2 hours: $title",
                     "Get ready — check your equipment")
            type == "RECOVERY" ->
                Pair("Rest day: $title",
                     "Take care of your body today")
            reminderMin >= 1440 ->
                Pair("Training tomorrow: $title",
                     "Review your goals before you sleep")
            else ->
                Pair("Training starts in 1 hour: $title",
                     "Warm up and prepare your mindset")
        }

        NotificationHelper.showReminderNotification(context, notifTitle, notifBody)
    }
}
