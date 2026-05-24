package com.shootermind.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shootermind.app.core.navigation.Routes
import com.shootermind.app.core.navigation.ShooterMindNavGraph
import com.shootermind.app.domain.model.ThemeMode
import com.shootermind.app.ui.components.BottomNavBar
import com.shootermind.app.ui.components.NavRailBar
import com.shootermind.app.ui.theme.ShooterMindTheme
import com.shootermind.app.ui.theme.ThemeViewModel

private val BOTTOM_NAV_ROUTES = setOf(
    Routes.HOME,
    Routes.SESSION_LIST,
    Routes.STATS,
    Routes.PROFILE
)

class MainActivity : ComponentActivity() {

    // ── Notification permission (Android 13+) ──────────────────────────────
    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — FCM still works for background */ }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        fetchAndStoreFcmToken()

        enableEdgeToEdge()
        setContent {
            // ── Theme ──────────────────────────────────────────────────────
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
            }

            ShooterMindTheme(darkTheme = isDark) {
                // ── Adaptive nav ───────────────────────────────────────────
                val windowSizeClass   = calculateWindowSizeClass(this)
                val useNavRail        = windowSizeClass.widthSizeClass != WindowWidthSizeClass.COMPACT

                val navController      = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute       = navBackStackEntry?.destination?.route
                val showNav            = currentRoute in BOTTOM_NAV_ROUTES

                Row(Modifier.fillMaxSize()) {
                    // Rail shown on medium (landscape phone) and expanded (tablet)
                    if (useNavRail && showNav) {
                        NavRailBar(navController = navController)
                    }

                    Scaffold(
                        modifier  = Modifier.fillMaxSize(),
                        bottomBar = {
                            // Bottom bar only on compact (phone portrait)
                            if (!useNavRail && showNav) {
                                BottomNavBar(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        ShooterMindNavGraph(
                            navController  = navController,
                            themeViewModel = themeViewModel,
                            modifier       = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun fetchAndStoreFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val uid = Firebase.auth.currentUser?.uid ?: return@addOnSuccessListener
            Firebase.firestore
                .collection("users").document(uid)
                .collection("fcmTokens").document("current")
                .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
        }
    }
}
