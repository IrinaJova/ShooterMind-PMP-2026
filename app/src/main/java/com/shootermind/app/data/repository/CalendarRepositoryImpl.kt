package com.shootermind.app.data.repository

import com.shootermind.app.data.local.dao.CalendarEventDao
import com.shootermind.app.data.local.entity.toDomain
import com.shootermind.app.data.local.entity.toEntity
import com.shootermind.app.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CalendarRepositoryImpl(
    private val dao: CalendarEventDao
) : CalendarRepository {

    override fun getAllEvents(userId: String): Flow<List<CalendarEvent>> =
        dao.getAllForUser(userId).map { list -> list.map { it.toDomain() } }

    override fun getUpcomingEvents(
        userId: String,
        fromMs: Long,
        limit : Int
    ): Flow<List<CalendarEvent>> =
        dao.getUpcomingForUser(userId, fromMs, limit).map { list -> list.map { it.toDomain() } }

    override suspend fun saveEvent(event: CalendarEvent) {
        dao.upsert(event.toEntity())
    }

    override suspend fun deleteEvent(id: String) {
        dao.deleteById(id)
    }

    override suspend fun getEventById(id: String): CalendarEvent? =
        dao.getById(id)?.toDomain()
}
