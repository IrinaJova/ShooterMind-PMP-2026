package com.shootermind.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shootermind.app.core.navigation.BottomNavItem
import com.shootermind.app.core.navigation.Routes

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick  = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Always pop back to HOME so the stack stays clean.
                            // For non-HOME destinations save/restore so the user
                            // doesn't lose their scroll position, etc.
                            // For HOME itself do NOT restore state — we always
                            // want a fresh landing, not a previously saved Stats stack.
                            popUpTo(Routes.HOME) {
                                saveState = item.route != Routes.HOME
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState    = item.route != Routes.HOME
                        }
                    }
                },
                icon  = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}
