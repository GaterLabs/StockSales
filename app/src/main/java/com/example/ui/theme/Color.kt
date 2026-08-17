package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// High Density Theme - Light Scheme Colors
val HighDensityPrimary = Color(0xFF5A3EAB)
val HighDensityOnPrimary = Color(0xFFFFFFFF)
val HighDensityPrimaryContainer = Color(0xFFE8DEFF)
val HighDensityOnPrimaryContainer = Color(0xFF190064)

val HighDensitySecondary = Color(0xFF5F5C71)
val HighDensityOnSecondary = Color(0xFFFFFFFF)
val HighDensitySecondaryContainer = Color(0xFFE5E1F9)
val HighDensityOnSecondaryContainer = Color(0xFF1B1A2C)

val HighDensityTertiary = Color(0xFF7B5267)
val HighDensityOnTertiary = Color(0xFFFFFFFF)
val HighDensityTertiaryContainer = Color(0xFFFFD8EC)
val HighDensityOnTertiaryContainer = Color(0xFF301123)

val HighDensityBackground = Color(0xFFF9F9FE)
val HighDensityOnBackground = Color(0xFF1B1B1F)
val HighDensitySurface = Color(0xFFFFFFFF)
val HighDensityOnSurface = Color(0xFF1B1B1F)
val HighDensitySurfaceVariant = Color(0xFFE5E1EC)
val HighDensityOnSurfaceVariant = Color(0xFF47464F)
val HighDensityOutline = Color(0xFF787680)
val HighDensityOutlineVariant = Color(0xFFC8C5D0)

// High Density Theme - Dark Scheme Colors (Vibrant, high-contrast)
val DarkHighDensityPrimary = Color(0xFFCFBCFF)
val DarkHighDensityOnPrimary = Color(0xFF391E7E)
val DarkHighDensityPrimaryContainer = Color(0xFF513896)
val DarkHighDensityOnPrimaryContainer = Color(0xFFE8DEFF)

val DarkHighDensitySecondary = Color(0xFFC8C5DD)
val DarkHighDensityOnSecondary = Color(0xFF302E42)
val DarkHighDensitySecondaryContainer = Color(0xFF5E5B73)
val DarkHighDensityOnSecondaryContainer = Color(0xFFE5E1F9)

val DarkHighDensityTertiary = Color(0xFFEEB8D3)
val DarkHighDensityOnTertiary = Color(0xFF482538)
val DarkHighDensityTertiaryContainer = Color(0xFF7A4F64)
val DarkHighDensityOnTertiaryContainer = Color(0xFFFFD8EC)

val DarkHighDensityBackground = Color(0xFF131318)
val DarkHighDensityOnBackground = Color(0xFFE4E1E6)
val DarkHighDensitySurface = Color(0xFF1B1B22)
val DarkHighDensityOnSurface = Color(0xFFE4E1E6)
val DarkHighDensitySurfaceVariant = Color(0xFF2C2C35)
val DarkHighDensityOnSurfaceVariant = Color(0xFFC8C5D0)
val DarkHighDensityOutline = Color(0xFF92909A)
val DarkHighDensityOutlineVariant = Color(0xFF47464F)

// High Density Status & Badge Accents (Standard Fallbacks)
val SuccessGreen = Color(0xFF15803D)
val WarningOrange = Color(0xFFB45309)
val ErrorRed = Color(0xFFB91C1C)
val InfoBlue = Color(0xFF1D4ED8)
val ProfitBadge = Color(0xFF047857)
val DebtBadge = Color(0xFFB91C1C)
val DenseCardBorder = Color(0xFFE0E0E0)
val DenseDarkCardBorder = Color(0xFF383842)

// Theme-Aware Dynamic Color Accessors (High contrast, WCAG compliant across all device displays)
object AppThemeColors {
    val isDark: Boolean
        @Composable
        get() = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val profitColor: Color
        @Composable
        get() = if (isDark) Color(0xFF34D399) else Color(0xFF047857)

    val debtColor: Color
        @Composable
        get() = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)

    val warningColor: Color
        @Composable
        get() = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)

    val successColor: Color
        @Composable
        get() = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)

    val infoColor: Color
        @Composable
        get() = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)

    val cardBorder: Color
        @Composable
        get() = if (isDark) Color(0xFF383842) else Color(0xFFE0E0E0)
}


