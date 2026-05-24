package com.shootermind.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shootermind.app.R
import com.shootermind.app.core.util.OnboardingPrefs
import com.shootermind.app.ui.auth.AuthViewModel
import com.shootermind.app.ui.profile.ProfileState
import com.shootermind.app.ui.profile.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateToHome         : () -> Unit,
    onNavigateToLogin        : () -> Unit,
    onNavigateToProfileSetup : () -> Unit,
    onNavigateToOnboarding   : () -> Unit,
    authViewModel    : AuthViewModel    = viewModel(),
    profileViewModel : ProfileViewModel = viewModel()
) {
    val context      = LocalContext.current
    val profileState by profileViewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        delay(1_200)

        // First launch — show onboarding
        if (!OnboardingPrefs.isCompleted(context)) {
            onNavigateToOnboarding()
            return@LaunchedEffect
        }

        if (!authViewModel.isLoggedIn) {
            onNavigateToLogin()
            return@LaunchedEffect
        }

        // Anonymous users skip profile setup
        if (profileViewModel.isAnonymous) {
            onNavigateToHome()
            return@LaunchedEffect
        }

        // Wait for Room to resolve the profile
        val resolved = profileViewModel.profileState
            .first { it !is ProfileState.Loading }

        when (resolved) {
            is ProfileState.Complete -> onNavigateToHome()
            else                     -> onNavigateToProfileSetup()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = stringResource(R.string.app_name),
            style      = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(40.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
