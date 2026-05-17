package com.shootermind.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary              = Purple40,
    onPrimary            = PurpleWhite,
    primaryContainer     = Purple90,
    onPrimaryContainer   = Purple10,
    secondary            = Lavender30,
    onSecondary          = PurpleWhite,
    secondaryContainer   = Lavender90,
    onSecondaryContainer = Purple10,
    tertiary             = Rose30,
    onTertiary           = PurpleWhite,
    tertiaryContainer    = Rose90,
    onTertiaryContainer  = Purple10,
    error                = Red40,
    onError              = PurpleWhite,
    errorContainer       = Red90,
    onErrorContainer     = Red10,
    background           = PurpleWhite,
    onBackground         = Grey10,
    surface              = PurpleWhite,
    onSurface            = Grey10,
    surfaceVariant       = NeutralVar90,
    onSurfaceVariant     = NeutralVar30,
    outline              = NeutralVar50,
)

private val DarkColors = darkColorScheme(
    primary              = Purple80,
    onPrimary            = Purple20,
    primaryContainer     = Purple30,
    onPrimaryContainer   = Purple90,
    secondary            = Lavender80,
    onSecondary          = Lavender30,
    secondaryContainer   = Lavender30,
    onSecondaryContainer = Lavender90,
    tertiary             = Rose80,
    onTertiary           = Rose30,
    tertiaryContainer    = Rose30,
    onTertiaryContainer  = Rose90,
    error                = Red80,
    onError              = Red10,
    errorContainer       = Red40,
    onErrorContainer     = Red90,
    background           = Grey10,
    onBackground         = Grey90,
    surface              = Grey10,
    onSurface            = Grey90,
    surfaceVariant       = NeutralVar30,
    onSurfaceVariant     = NeutralVar80,
    outline              = NeutralVar60,
)

@Composable
fun ShooterMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled so our pastel purple palette always shows
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
