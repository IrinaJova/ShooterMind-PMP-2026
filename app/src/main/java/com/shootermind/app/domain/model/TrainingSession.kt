package com.shootermind.app.domain.model

data class TrainingSession(
    val id             : String,
    val userId         : String,
    val discipline     : Discipline,
    val dateMs         : Long,
    val totalScore     : Double,
    val shotCount      : Int,
    val notes          : String  = "",
    // "98.7|97.3|99.1|…" — pipe-separated series totals (or shot-by-shot data legacy)
    val seriesData     : String  = "",
    val durationMinutes: Int     = 0,
    val photoUri       : String? = null,
    // Reflection (legacy — kept for migration compat)
    val learnedToday   : String  = "",
    val mistakesMade   : String  = "",
    val improvements   : String  = "",

    // ── New fields (v4) ────────────────────────────────────────────────────
    // Time (-1 = not set)
    val startHour      : Int     = -1,
    val startMinute    : Int     = 0,
    val endHour        : Int     = -1,
    val endMinute      : Int     = 0,
    // Session type
    val isCompetition  : Boolean = false,
    val isControlSession: Boolean = false,
    // Result options
    val useDecimalScore: Boolean = true,
    val splitIntoSeries: Boolean = true,
    val seriesCount    : Int     = 6,
    val shotsPerSeries : Int     = 10,
    // Journal
    val batch          : String  = "",
    val airPressure    : String  = "",
    // Emoji ratings 0 = unset, 1–5
    val muscleRecovery : Int     = 0,
    val fatigue        : Int     = 0,
    val concentration  : Int     = 0,
    val endurance      : Int     = 0,
    val heartRate      : Int     = 0
)
