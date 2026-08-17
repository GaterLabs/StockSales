package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkHighDensityPrimary,
    onPrimary = DarkHighDensityOnPrimary,
    primaryContainer = DarkHighDensityPrimaryContainer,
    onPrimaryContainer = DarkHighDensityOnPrimaryContainer,
    secondary = DarkHighDensitySecondary,
    onSecondary = DarkHighDensityOnSecondary,
    secondaryContainer = DarkHighDensitySecondaryContainer,
    onSecondaryContainer = DarkHighDensityOnSecondaryContainer,
    tertiary = DarkHighDensityTertiary,
    onTertiary = DarkHighDensityOnTertiary,
    tertiaryContainer = DarkHighDensityTertiaryContainer,
    onTertiaryContainer = DarkHighDensityOnTertiaryContainer,
    background = DarkHighDensityBackground,
    onBackground = DarkHighDensityOnBackground,
    surface = DarkHighDensitySurface,
    onSurface = DarkHighDensityOnSurface,
    surfaceVariant = DarkHighDensitySurfaceVariant,
    onSurfaceVariant = DarkHighDensityOnSurfaceVariant,
    outline = DarkHighDensityOutline,
    outlineVariant = DarkHighDensityOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondary = HighDensitySecondary,
    onSecondary = HighDensityOnSecondary,
    secondaryContainer = HighDensitySecondaryContainer,
    onSecondaryContainer = HighDensityOnSecondaryContainer,
    tertiary = HighDensityTertiary,
    onTertiary = HighDensityOnTertiary,
    tertiaryContainer = HighDensityTertiaryContainer,
    onTertiaryContainer = HighDensityOnTertiaryContainer,
    background = HighDensityBackground,
    onBackground = HighDensityOnBackground,
    surface = HighDensitySurface,
    onSurface = HighDensityOnSurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityOnSurfaceVariant,
    outline = HighDensityOutline,
    outlineVariant = HighDensityOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
