package com.shootermind.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shootermind.app.data.local.entity.TrainingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSessionDao {

    @Query("SELECT * FROM training_sessions WHERE userId = :userId ORDER BY dateMs DESC")
    fun getAllByUser(userId: String): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE userId = :userId ORDER BY dateMs DESC LIMIT :limit")
    fun getRecentByUser(userId: String, limit: Int): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun getById(id: String): TrainingSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TrainingSessionEntity)

    @Delete
    suspend fun delete(session: TrainingSessionEntity)
}
