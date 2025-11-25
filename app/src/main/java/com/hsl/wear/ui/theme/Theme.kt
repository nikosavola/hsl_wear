package com.hsl.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.ColorScheme

// --- HSL BRAND COLORS ---
val HslBlue = Color(0xFF007AC9)      // The iconic HSL Blue
val HslDarkBlue = Color(0xFF005A94)
val HslLightBlue = Color(0xFF4DA6FF)
val HslAccentBlue = Color(0xFF0099FF)

// --- ACCESSIBILITY COLORS ---
val HighContrastWhite = Color(0xFFFFFFFF)
val HighContrastBlack = Color(0xFF000000)

// --- ENHANCED TYPOGRAPHY ---
@Composable
fun HslTypography() = Typography().let { defaultTypography ->
    defaultTypography.copy(
        // Enhance readability on small screens
        displaySmall = defaultTypography.displaySmall.copy(
            letterSpacing = (-0.5).sp
        ),
        titleMedium = defaultTypography.titleMedium.copy(
            letterSpacing = (-0.25).sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        ),
        labelSmall = defaultTypography.labelSmall.copy(
            letterSpacing = 0.1.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        ),
        labelMedium = defaultTypography.labelMedium.copy(
            letterSpacing = 0.05.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    )
}

// --- HYBRID MATERIAL YOU THEME ---
@Composable
fun HslWearTheme(
    content: @Composable () -> Unit
) {
    // Create hybrid color scheme that enhances contrast while maintaining HSL brand
    val colorScheme = createEnhancedHslColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HslTypography(),
        content = content
    )
}

@Composable
private fun createEnhancedHslColorScheme(): ColorScheme {
    return ColorScheme(
        // HSL Blue as primary - maintains brand identity
        primary = HslBlue,
        primaryDim = HslDarkBlue,
        primaryContainer = HslDarkBlue,
        onPrimary = HighContrastWhite,
        onPrimaryContainer = HighContrastWhite,

        // Enhanced secondary colors with better contrast
        secondary = Color(0xFF6B7280),
        secondaryDim = Color(0xFF4B5563),
        secondaryContainer = Color(0xFF374151),
        onSecondary = HighContrastWhite,
        onSecondaryContainer = Color(0xFFD1D5DB),

        // HSL Accent for tertiary - provides brand consistency
        tertiary = HslAccentBlue,
        tertiaryDim = HslBlue,
        tertiaryContainer = HslLightBlue.copy(alpha = 0.3f),
        onTertiary = HighContrastWhite,
        onTertiaryContainer = HslDarkBlue,

        // Enhanced surface colors for better readability
        surfaceContainer = Color(0xFF1F2937),
        surfaceContainerLow = Color(0xFF111827),
        surfaceContainerHigh = Color(0xFF374151),

        // High contrast text colors
        onSurface = HighContrastWhite,
        onSurfaceVariant = Color(0xFFD1D5DB),

        // Improved outline visibility
        outline = Color(0xFF6B7280),
        outlineVariant = Color(0xFF4B5563),

        // Dark background for Wear OS
        background = Color(0xFF000000),
        onBackground = HighContrastWhite,

        // Enhanced error colors with better accessibility
        error = Color(0xFFDC2626),
        errorContainer = Color(0xFF7F1D1D),
        onError = HighContrastWhite,
        onErrorContainer = Color(0xFFFCA5A5)
    )
}
