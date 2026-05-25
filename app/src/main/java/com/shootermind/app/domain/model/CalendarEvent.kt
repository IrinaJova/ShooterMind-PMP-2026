package com.shootermind.app.domain.model

enum class EventType { TRAINING, COMPETITION, RECOVERY, OTHER }

/** Reminder offsets stored in minutes */
object ReminderOffset {
    const val MINS_30   =   30
    const val HOUR_1    =   60
    const val HOURS_2   =  120
    const val DAY_1     = 1440
}

data class CalendarEvent(
    val id                : String,
    val userId            : String,
    val title             : String,
    val eventType         : EventType,
    val dateTimeMs        : Long,
    val location          : String,
    val discipline        : Discipline?,
    val goalScore         : Double,
    val notes             : String,
    val reminderEnabled   : Boolean,
    val reminderMinsBefore: Int,
    val createdAt         : Long,
    val updatedAt         : Long
)
