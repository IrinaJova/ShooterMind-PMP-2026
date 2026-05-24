package com.shootermind.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shootermind.app.data.local.dao.TrainingSessionDao
import com.shootermind.app.data.local.entity.TrainingSessionEntity

@Database(
    entities  = [TrainingSessionEntity::class],
    version   = 1,
    exportSchema = false
)
abstract class ShooterMindDatabase : RoomDatabase() {

    abstract fun trainingSessionDao(): TrainingSessionDao

    companion object {
        @Volatile
        private var INSTANCE: ShooterMindDatabase? = null

        fun getDatabase(context: Context): ShooterMindDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShooterMindDatabase::class.java,
                    "shootermind_db"
                ).build().also { INSTANCE = it }
            }
    }
}
