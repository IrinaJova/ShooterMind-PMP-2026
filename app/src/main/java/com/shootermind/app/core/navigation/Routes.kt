package com.shootermind.app.core.navigation

object Routes {
    const val SPLASH        = "splash"
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val HOME          = "home"
    const val SESSION_LIST  = "session_list"
    const val NEW_SESSION   = "new_session"
    const val STATS         = "stats"
    const val PROFILE       = "profile"
    const val SETTINGS      = "settings"

    // Session detail added in Phase 4 alongside full session implementation
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    fun sessionDetail(sessionId: String) = "session_detail/$sessionId"
}
