package com.shootermind.app.data.repository

import com.shootermind.app.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getAllEvents(userId: String): Flow<List<CalendarEvent>>
    fun getUpcomingEvents(userId: String, fromMs: Long, limit: Int = 10): Flow<List<CalendarEvent>>
    suspend fun saveEvent(event: CalendarEvent)
    suspend fun deleteEvent(id: String)
    suspend fun getEventById(id: String): CalendarEvent?
}
