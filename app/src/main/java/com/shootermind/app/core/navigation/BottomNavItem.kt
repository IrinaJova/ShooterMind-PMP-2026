package com.shootermind.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.shootermind.app.R

enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    HOME(
        route    = Routes.HOME,
        labelRes = R.string.nav_home,
        icon     = Icons.Default.Home
    ),
    SESSIONS(
        route    = Routes.SESSION_LIST,
        labelRes = R.string.nav_sessions,
        icon     = Icons.Default.DateRange
    ),
    CALENDAR(
        route    = Routes.CALENDAR,
        labelRes = R.string.nav_calendar,
        icon     = Icons.Default.CalendarMonth
    ),
    STATS(
        route    = Routes.STATS,
        labelRes = R.string.nav_stats,
        icon     = Icons.Default.BarChart
    ),
    PROFILE(
        route    = Routes.PROFILE,
        labelRes = R.string.nav_profile,
        icon     = Icons.Default.Person
    )
}
