package com.shootermind.app.core.navigation

object Routes {
    const val SPLASH        = "splash"
    const val ONBOARDING    = "onboarding"
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val HOME          = "home"
    const val SESSION_LIST  = "session_list"
    const val NEW_SESSION   = "new_session"
    const val STATS         = "stats"
    const val PROFILE       = "profile"
    const val SETTINGS      = "settings"
    const val PROFILE_SETUP = "profile_setup"
    const val CALENDAR      = "calendar"
    const val EDIT_PROFILE  = "edit_profile"

    const val SESSION_DETAIL = "session_detail/{sessionId}"
    fun sessionDetail(sessionId: String) = "session_detail/$sessionId"

    const val ADD_EVENT      = "add_event"
    const val EDIT_EVENT     = "edit_event/{eventId}"
    fun editEvent(eventId: String) = "edit_event/$eventId"
}
