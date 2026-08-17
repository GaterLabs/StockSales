package com.example.ui.util

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇺🇸"),
    INDONESIAN("id", "Bahasa Indonesia", "🇮🇩")
}

enum class AppThemeMode(val key: String, val displayNameEn: String, val displayNameId: String) {
    SYSTEM("SYSTEM", "System Default", "Ikuti Sistem HP"),
    LIGHT("LIGHT", "Light Mode", "Mode Terang"),
    DARK("DARK", "Dark Mode", "Mode Gelap")
}

val LocalAppStrings = compositionLocalOf<AppStrings> { AppStringsEn }
val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
