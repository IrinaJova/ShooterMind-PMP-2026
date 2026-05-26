package com.shootermind.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shootermind.app.ui.auth.screens.LoginScreen
import com.shootermind.app.ui.auth.screens.RegisterScreen
import com.shootermind.app.ui.calendar.CalendarViewModel
import com.shootermind.app.ui.calendar.screens.AddEditEventScreen
import com.shootermind.app.ui.calendar.screens.CalendarScreen
import com.shootermind.app.ui.home.HomeScreen
import com.shootermind.app.ui.onboarding.OnboardingScreen
import com.shootermind.app.ui.profile.EditProfileScreen
import com.shootermind.app.ui.profile.ProfileScreen
import com.shootermind.app.ui.profile.ProfileViewModel
import com.shootermind.app.ui.profile.setup.ProfileSetupScreen
import com.shootermind.app.ui.session.SessionViewModel
import com.shootermind.app.ui.session.screens.NewSessionScreen
import com.shootermind.app.ui.session.screens.SessionDetailsScreen
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
    // Activity-scoped ViewModels
    val sessionViewModel : SessionViewModel  = viewModel()
    val profileViewModel : ProfileViewModel  = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()

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
                onNavigateToOnboarding   = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                profileViewModel = profileViewModel
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
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

        // ── Profile Setup ─────────────────────────────────────────────────
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
                onSessionClick    = { id -> navController.navigate(Routes.sessionDetail(id)) },
                onViewStats       = { navController.navigate(Routes.STATS) },
                onOpenCalendar    = { navController.navigate(Routes.CALENDAR) },
                sessionViewModel  = sessionViewModel,
                profileViewModel  = profileViewModel,
                calendarViewModel = calendarViewModel
            )
        }

        composable(Routes.SESSION_LIST) {
            SessionListScreen(
                onStartNewSession = { navController.navigate(Routes.NEW_SESSION) },
                onSessionClick    = { id -> navController.navigate(Routes.sessionDetail(id)) },
                sessionViewModel  = sessionViewModel
            )
        }

        composable(Routes.NEW_SESSION) {
            NewSessionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.SESSION_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionDetailsScreen(
                sessionId        = sessionId,
                sessionViewModel = sessionViewModel,
                onNavigateBack   = { navController.popBackStack() }
            )
        }

        // ── Calendar ──────────────────────────────────────────────────────
        composable(Routes.CALENDAR) {
            CalendarScreen(
                onAddEvent        = { navController.navigate(Routes.ADD_EVENT) },
                onEditEvent       = { id -> navController.navigate(Routes.editEvent(id)) },
                calendarViewModel = calendarViewModel
            )
        }

        composable(Routes.ADD_EVENT) {
            AddEditEventScreen(
                eventId           = null,
                onNavigateBack    = { navController.popBackStack() },
                calendarViewModel = calendarViewModel
            )
        }

        composable(
            route     = Routes.EDIT_EVENT,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            AddEditEventScreen(
                eventId           = eventId,
                onNavigateBack    = { navController.popBackStack() },
                calendarViewModel = calendarViewModel
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

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                profileViewModel = profileViewModel,
                onNavigateBack   = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val navigateToLogin: () -> Unit = {
                profileViewModel.signOut()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
            SettingsScreen(
                themeViewModel   = themeViewModel,
                onNavigateBack   = { navController.popBackStack() },
                onEditProfile    = { navController.navigate(Routes.EDIT_PROFILE) },
                onSignOut        = navigateToLogin,
                onAccountDeleted = navigateToLogin
            )
        }
    }
}
