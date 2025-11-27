package com.hsl.wear.utils.constants

import androidx.compose.ui.graphics.Color

/**
 * Color constants for the HSL Wear app theme and transport modes.
 */
object ColorConstants {

    // HSL Brand Colors
    val HSL_BLUE = Color(0xFF007AC9)
    val HSL_DARK_BLUE = Color(0xFF005A94)
    val HSL_LIGHT_BLUE = Color(0xFF4DA6FF)
    val HSL_ACCENT_BLUE = Color(0xFF0099FF)

    // Transport Mode Colors
    val BUS_COLOR = Color(0xFF007AC9)      // HSL Blue
    val TRAM_COLOR = Color(0xFF00C853)     // Green
    val METRO_COLOR = Color(0xFFFF6F00)    // Orange
    val TRAIN_COLOR = Color(0xFF7B1FA2)    // Purple
    val FERRY_COLOR = Color(0xFF0091EA)    // Light Blue
    val WALK_COLOR = Color(0xFF9E9E9E)     // Gray

    // High Contrast Colors
    val HIGH_CONTRAST_WHITE = Color(0xFFFFFFFF)
    val HIGH_CONTRAST_BLACK = Color(0xFF000000)

    // Theme Colors (Surface Variant)
    val SECONDARY_COLOR = Color(0xFF6B7280)
    val SECONDARY_DIM = Color(0xFF4B5563)
    val SECONDARY_CONTAINER = Color(0xFF374151)

    // Surface Colors
    val SURFACE_CONTAINER = Color(0xFF1F2937)
    val SURFACE_CONTAINER_LOW = Color(0xFF111827)
    val SURFACE_CONTAINER_HIGH = Color(0xFF374151)

    // On Surface Colors
    val ON_SURFACE_VARIANT = Color(0xFFD1D5DB)

    // Outline Colors
    val OUTLINE_COLOR = Color(0xFF6B7280)
    val OUTLINE_VARIANT = Color(0xFF4B5563)

    // Background Colors
    val BACKGROUND_COLOR = Color(0xFF000000)

    // Error Colors
    val ERROR_COLOR = Color(0xFFDC2626)
    val ERROR_CONTAINER = Color(0xFF7F1D1D)
    val ERROR_CONTAINER_TEXT = Color(0xFFFCA5A5)

    // State Colors for Transport Mode Icons
    val TRANSPORT_MODE_ACTIVE_ALPHA = 1.0f
    val TRANSPORT_MODE_INACTIVE_ALPHA = 0.7f

    // Progress Indicator Colors
    val PROGRESS_COLOR = HSL_BLUE
    val PROGRESS_BACKGROUND_COLOR = Color(0xFF374151)

    // Card Colors
    val CARD_CONTAINER_COLOR = Color(0xFF1F2937)
    val CARD_CONTAINER_HOVER = Color(0xFF374151)
}