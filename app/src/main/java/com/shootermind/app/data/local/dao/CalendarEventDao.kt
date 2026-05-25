package com.shootermind.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shootermind.app.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events WHERE userId = :userId ORDER BY dateTimeMs ASC")
    fun getAllForUser(userId: String): Flow<List<CalendarEventEntity>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE userId = :userId AND dateTimeMs >= :fromMs
        ORDER BY dateTimeMs ASC
        LIMIT :limit
    """)
    fun getUpcomingForUser(
        userId: String,
        fromMs: Long,
        limit : Int = 10
    ): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM calendar_events WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CalendarEventEntity?
}
