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

        val timeLabel = when {
            reminderMin >= 1440 -> "tomorrow"
            reminderMin >= 120  -> "in ${reminderMin / 60}h"
            reminderMin >= 60   -> "in 1 hour"
            else                -> "in ${reminderMin} min"
        }

        val (notifTitle, notifBody) = when (type) {
            "COMPETITION" -> when {
                reminderMin >= 1440 ->
                    Pair("Competition tomorrow: $title",
                         "Prepare your equipment and focus plan")
                reminderMin >= 120 ->
                    Pair("Competition in ${reminderMin / 60}h: $title",
                         "Get ready — check your equipment")
                else ->
                    Pair("Competition $timeLabel: $title",
                         "Focus up — competition time is near")
            }
            "RECOVERY" ->
                Pair("Recovery: $title",
                     "Take care of your body today")
            "OTHER" ->
                Pair("Upcoming: $title",
                     "You have an event $timeLabel")
            else -> // TRAINING
                when {
                    reminderMin >= 1440 ->
                        Pair("Training tomorrow: $title",
                             "Review your goals before you sleep")
                    else ->
                        Pair("Training $timeLabel: $title",
                             "Warm up and prepare your mindset")
                }
        }

        NotificationHelper.showReminderNotification(context, notifTitle, notifBody)
    }
}
