package com.shootermind.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shootermind.app.ui.auth.screens.LoginScreen
import com.shootermind.app.ui.auth.screens.RegisterScreen
import com.shootermind.app.ui.home.HomeScreen
import com.shootermind.app.ui.profile.ProfileScreen
import com.shootermind.app.ui.profile.ProfileViewModel
import com.shootermind.app.ui.profile.setup.ProfileSetupScreen
import com.shootermind.app.ui.session.SessionViewModel
import com.shootermind.app.ui.session.screens.NewSessionScreen
import com.shootermind.app.ui.session.screens.SessionListScreen
import com.shootermind.app.ui.settings.SettingsScreen
import com.shootermind.app.ui.splash.SplashScreen
import com.shootermind.app.ui.stats.StatsScreen
import com.shootermind.app.ui.theme.ThemeViewModel

@Composable
fun ShooterMindNavGraph(
    navController : NavHostController,
    themeViewModel: ThemeViewModel,
    modifier      : Modifier = Modifier
) {
    // Activity-scoped ViewModels — shared across destinations
    val sessionViewModel: SessionViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController    = navController,
        startDestination = Routes.SPLASH,
        modifier         = modifier
    ) {

        // ── Splash ────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToHome         = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin        = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                profileViewModel = profileViewModel
            )
        }

        // ── Auth ──────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess       = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Profile Setup (shown after first login) ───────────────────────
        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                profileViewModel = profileViewModel,
                onSetupComplete  = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }

        // ── Main tabs ─────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onStartNewSession = { navController.navigate(Routes.NEW_SESSION) },
                sessionViewModel  = sessionViewModel
            )
        }

        composable(Routes.SESSION_LIST) {
            SessionListScreen(
                onStartNewSession = { navController.navigate(Routes.NEW_SESSION) },
                sessionViewModel  = sessionViewModel
            )
        }

        composable(Routes.NEW_SESSION) {
            NewSessionScreen(
                onNavigateBack   = { navController.popBackStack() },
                sessionViewModel = sessionViewModel
            )
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                profileViewModel     = profileViewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onSignOut            = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
