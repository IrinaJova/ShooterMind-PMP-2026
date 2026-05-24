package com.shootermind.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shootermind.app.data.local.dao.TrainingSessionDao
import com.shootermind.app.data.local.dao.UserProfileDao
import com.shootermind.app.data.local.entity.TrainingSessionEntity
import com.shootermind.app.data.local.entity.UserProfileEntity

@Database(
    entities     = [TrainingSessionEntity::class, UserProfileEntity::class],
    version      = 2,
    exportSchema = false
)
abstract class ShooterMindDatabase : RoomDatabase() {

    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ShooterMindDatabase? = null

        fun getDatabase(context: Context): ShooterMindDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShooterMindDatabase::class.java,
                    "shootermind_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { INSTANCE = it }
            }
    }
}
