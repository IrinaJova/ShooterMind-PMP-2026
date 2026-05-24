package com.shootermind.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shootermind.app.core.navigation.Routes
import com.shootermind.app.core.navigation.ShooterMindNavGraph
import com.shootermind.app.ui.components.BottomNavBar
import com.shootermind.app.ui.theme.ShooterMindTheme

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
            ShooterMindTheme {
                val windowSizeClass    = calculateWindowSizeClass(this)
                val navController      = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute       = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier  = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in BOTTOM_NAV_ROUTES) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    ShooterMindNavGraph(
                        navController = navController,
                        modifier      = Modifier.padding(innerPadding)
                    )
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
