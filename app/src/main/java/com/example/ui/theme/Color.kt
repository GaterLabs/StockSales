package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Supabase Color Scheme - Light
val HighDensityPrimary = Color(0xFF3ECF8E)
val HighDensityOnPrimary = Color(0xFF171717)

val HighDensityPrimaryContainer = Color(0xFFD1FAE5)
val HighDensityOnPrimaryContainer = Color(0xFF0F2C20)

val HighDensitySecondary = Color(0xFFEDEDED)
val HighDensityOnSecondary = Color(0xFF171717)

val HighDensitySecondaryContainer = Color(0xFFF5F5F5)
val HighDensityOnSecondaryContainer = Color(0xFF171717)

val HighDensityTertiary = Color(0xFF6B5BD2)
val HighDensityOnTertiary = Color(0xFFFFFFFF)

val HighDensityTertiaryContainer = Color(0xFFEDE8FF)
val HighDensityOnTertiaryContainer = Color(0xFF1C0F5B)

val HighDensityBackground = Color(0xFFFFFFFF)
val HighDensityOnBackground = Color(0xFF171717)

val HighDensitySurface = Color(0xFFFAFAFA)
val HighDensityOnSurface = Color(0xFF171717)

val HighDensitySurfaceVariant = Color(0xFFF0F0F0)
val HighDensityOnSurfaceVariant = Color(0xFF707070)

val HighDensityOutline = Color(0xFFDFDFDF)
val HighDensityOutlineVariant = Color(0xFFC7C7C7)

val HighDensityError = Color(0xFFEF4444)
val HighDensityOnError = Color(0xFFFFFFFF)
val HighDensityErrorContainer = Color(0xFFFEF2F2)
val HighDensityOnErrorContainer = Color(0xFF991B1B)

val HighDensityInverseSurface = Color(0xFF171717)
val HighDensityInverseOnSurface = Color(0xFFFAFAFA)

// Supabase Color Scheme - Dark
val DarkHighDensityPrimary = Color(0xFF3ECF8E)
val DarkHighDensityOnPrimary = Color(0xFF0A0A0A)

val DarkHighDensityPrimaryContainer = Color(0xFF0F2C20)
val DarkHighDensityOnPrimaryContainer = Color(0xFF3ECF8E)

val DarkHighDensitySecondary = Color(0xFF292929)
val DarkHighDensityOnSecondary = Color(0xFFEDEDED)

val DarkHighDensitySecondaryContainer = Color(0xFF212121)
val DarkHighDensityOnSecondaryContainer = Color(0xFFEDEDED)

val DarkHighDensityTertiary = Color(0xFF9B8AFF)
val DarkHighDensityOnTertiary = Color(0xFF1C0F5B)

val DarkHighDensityTertiaryContainer = Color(0xFF2A2060)
val DarkHighDensityOnTertiaryContainer = Color(0xFFD4C5FF)

val DarkHighDensityBackground = Color(0xFF121212)
val DarkHighDensityOnBackground = Color(0xFFEDEDED)

val DarkHighDensitySurface = Color(0xFF171717)
val DarkHighDensityOnSurface = Color(0xFFEDEDED)

val DarkHighDensitySurfaceVariant = Color(0xFF1F1F1F)
val DarkHighDensityOnSurfaceVariant = Color(0xFFA0A0A0)

val DarkHighDensityOutline = Color(0xFF2E2E2E)
val DarkHighDensityOutlineVariant = Color(0xFF3E3E3E)

val DarkHighDensityError = Color(0xFFEF4444)
val DarkHighDensityOnError = Color(0xFFFFFFFF)
val DarkHighDensityErrorContainer = Color(0xFF450A0A)
val DarkHighDensityOnErrorContainer = Color(0xFFFCA5A5)

val DarkHighDensityInverseSurface = Color(0xFFEDEDED)
val DarkHighDensityInverseOnSurface = Color(0xFF171717)

// Status & Badge Accents
val SuccessGreen = Color(0xFF15803D)
val WarningOrange = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3E63DD)
val ProfitBadge = Color(0xFF00C46B)
val DebtBadge = Color(0xFFEF4444)
val DenseCardBorder = Color(0xFFDFDFDF)
val DenseDarkCardBorder = Color(0xFF2E2E2E)

// Theme-Aware Dynamic Color Accessors
object AppThemeColors {
    val isDark: Boolean
        @Composable
        get() = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val profitColor: Color
        @Composable
        get() = if (isDark) Color(0xFF3ECF8E) else Color(0xFF00C46B)

    val debtColor: Color
        @Composable
        get() = if (isDark) Color(0xFFFCA5A5) else Color(0xFFEF4444)

    val warningColor: Color
        @Composable
        get() = if (isDark) Color(0xFFFBBF24) else Color(0xFFF59E0B)

    val successColor: Color
        @Composable
        get() = if (isDark) Color(0xFF3ECF8E) else Color(0xFF15803D)

    val infoColor: Color
        @Composable
        get() = if (isDark) Color(0xFF9B8AFF) else Color(0xFF3E63DD)

    val cardBorder: Color
        @Composable
        get() = if (isDark) Color(0xFF2E2E2E) else Color(0xFFDFDFDF)
}
