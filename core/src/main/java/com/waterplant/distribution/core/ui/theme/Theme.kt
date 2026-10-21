package com.waterplant.distribution.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = SecondaryTeal,
    onSecondary = SurfaceLight,
    secondaryContainer = SecondaryTealLight,
    onSecondaryContainer = SecondaryTealDark,
    tertiary = WarningAmber,
    onTertiary = SurfaceLight,
    tertiaryContainer = WarningAmberLight,
    onTertiaryContainer = WarningAmber,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = BackgroundLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    errorContainer = ErrorRedLight,
    onError = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = PrimaryBlueDark,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,
    secondary = SecondaryTealLight,
    onSecondary = SecondaryTealDark,
    secondaryContainer = SecondaryTealDark,
    onSecondaryContainer = SecondaryTealLight,
    tertiary = WarningAmberLight,
    onTertiary = WarningAmber,
    tertiaryContainer = WarningAmber,
    onTertiaryContainer = WarningAmberLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = BackgroundDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRedLight,
    errorContainer = ErrorRed,
    onError = ErrorRedDark
)

@Composable
fun WaterJarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
