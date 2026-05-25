package com.shootermind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shootermind.app.domain.model.CalendarEvent
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.EventType

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id                : String,
    val userId            : String,
    val title             : String,
    val eventType         : String,       // EventType.name
    val dateTimeMs        : Long,
    val location          : String,
    val disciplineName    : String?,      // Discipline.name or null
    val goalScore         : Double,
    val notes             : String,
    val reminderEnabled   : Boolean,
    val reminderMinsBefore: Int,
    val createdAt         : Long,
    val updatedAt         : Long
)

fun CalendarEventEntity.toDomain() = CalendarEvent(
    id                 = id,
    userId             = userId,
    title              = title,
    eventType          = EventType.valueOf(eventType),
    dateTimeMs         = dateTimeMs,
    location           = location,
    discipline         = disciplineName?.let { runCatching { Discipline.valueOf(it) }.getOrNull() },
    goalScore          = goalScore,
    notes              = notes,
    reminderEnabled    = reminderEnabled,
    reminderMinsBefore = reminderMinsBefore,
    createdAt          = createdAt,
    updatedAt          = updatedAt
)

fun CalendarEvent.toEntity() = CalendarEventEntity(
    id                 = id,
    userId             = userId,
    title              = title,
    eventType          = eventType.name,
    dateTimeMs         = dateTimeMs,
    location           = location,
    disciplineName     = discipline?.name,
    goalScore          = goalScore,
    notes              = notes,
    reminderEnabled    = reminderEnabled,
    reminderMinsBefore = reminderMinsBefore,
    createdAt          = createdAt,
    updatedAt          = updatedAt
)
