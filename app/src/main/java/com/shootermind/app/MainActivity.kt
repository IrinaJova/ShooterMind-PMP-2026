package com.shootermind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shootermind.app.core.navigation.Routes
import com.shootermind.app.core.navigation.ShooterMindNavGraph
import com.shootermind.app.ui.components.BottomNavBar
import com.shootermind.app.ui.theme.ShooterMindTheme

// @AndroidEntryPoint added in Phase 2 when Hilt is introduced

// Routes where the bottom navigation bar should be visible
private val BOTTOM_NAV_ROUTES = setOf(
    Routes.HOME,
    Routes.SESSION_LIST,
    Routes.STATS,
    Routes.PROFILE
)

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShooterMindTheme {
                // windowSizeClass drives adaptive layouts in Phase 9 (tablet / landscape)
                val windowSizeClass = calculateWindowSizeClass(this)

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
}
