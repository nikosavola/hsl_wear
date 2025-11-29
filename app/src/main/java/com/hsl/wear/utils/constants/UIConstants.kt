package com.hsl.wear.utils.constants

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI-related constants for dimensions, spacing, text sizes, and layout values.
 */
object UIConstants {
    // Button Dimensions
    val BUTTON_HEIGHT = 52.dp
    val BUTTON_CORNER_RADIUS = 26.dp
    val BUTTON_SCALE_PRESSED = 0.95f

    val PROGRESS_INDICATOR_SIZE = 16.dp
    val PROGRESS_INDICATOR_STROKE_WIDTH = 2.dp

    // Spacing Values
    val SPACING_MEDIUM = 8.dp

    // Animation Constants
    val ANIMATION_DURATION_MS = 200  // Button animation duration
    val ANIMATION_EASING = androidx.compose.animation.core.EaseOutCubic
}