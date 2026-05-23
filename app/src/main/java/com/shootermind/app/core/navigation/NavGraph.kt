package com.shootermind.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shootermind.app.ui.auth.screens.LoginScreen
import com.shootermind.app.ui.auth.screens.RegisterScreen
import com.shootermind.app.ui.home.HomeScreen
import com.shootermind.app.ui.profile.ProfileScreen
import com.shootermind.app.ui.session.screens.NewSessionScreen
import com.shootermind.app.ui.session.screens.SessionListScreen
import com.shootermind.app.ui.settings.SettingsScreen
import com.shootermind.app.ui.splash.SplashScreen
import com.shootermind.app.ui.stats.StatsScreen

@Composable
fun ShooterMindNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.SPLASH,
        modifier         = modifier
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess       = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin  = { navController.popBackStack() },
                onRegisterSuccess  = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Main (bottom-nav tabs) ────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(onStartNewSession = { navController.navigate(Routes.NEW_SESSION) })
        }

        composable(Routes.SESSION_LIST) {
            SessionListScreen(onStartNewSession = { navController.navigate(Routes.NEW_SESSION) })
        }

        composable(Routes.NEW_SESSION) {
            NewSessionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
        }

        // ── Settings (pushed from Profile) ────────────────────────────────────
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
