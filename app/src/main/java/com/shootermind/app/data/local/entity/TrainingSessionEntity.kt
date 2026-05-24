package com.shootermind.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey val id             : String,
    val userId         : String,
    val discipline     : String,          // "AIR_RIFLE" | "AIR_PISTOL"
    val dateMs         : Long,
    val totalScore     : Double,
    val shotCount      : Int,
    val notes          : String  = "",
    val seriesData     : String  = "",    // "seriesTotal1|seriesTotal2|…"
    val durationMinutes: Int     = 0,
    val photoUri       : String? = null,
    val learnedToday   : String  = "",
    val mistakesMade   : String  = "",
    val improvements   : String  = "",

    // ── v4 columns ─────────────────────────────────────────────────────────
    val startHour       : Int     = -1,
    val startMinute     : Int     = 0,
    val endHour         : Int     = -1,
    val endMinute       : Int     = 0,
    val isCompetition   : Boolean = false,
    val isControlSession: Boolean = false,
    val useDecimalScore : Boolean = true,
    val splitIntoSeries : Boolean = true,
    val seriesCount     : Int     = 6,
    val shotsPerSeries  : Int     = 10,
    val batch           : String  = "",
    val airPressure     : String  = "",
    val muscleRecovery  : Int     = 0,
    val fatigue         : Int     = 0,
    val concentration   : Int     = 0,
    val endurance       : Int     = 0,
    val heartRate       : Int     = 0
)
